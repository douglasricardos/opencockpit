# 🛠️ SDD — System Design Document
**Projeto:** `OpenCockpit` / `meucarro-cockpit` (Cockpit Inteligente Honda HR-V 2017)  
**Versão:** 1.1.0-final  
**Linguagem & Framework Base:** Kotlin / Jetpack Compose (Android Automotive / Head Unit Landscape)  
**Hardware Alvo:** Central Multimídia K4811 NWD (1280x720 Landscape)  
**Status dos Itens:** 🟡 PLANEJADO  

---

## 1. Arquitetura de Alto Nível

A solução opera em três camadas desacopladas (Hexagonal / Clean Architecture):
1. **Camada de Sistema e Boot (Nível OS):** Baseada nas ferramentas do repositório `douglasricardos/MULTIMIDIA-ANDROID` para persistência de assets de boot no Android interno da multimídia.
2. **Camada de Apresentação (UI / Jetpack Compose):** Interface reativa de 60 FPS, adaptada para telas panorâmicas (1280x720), dividida em Grid Assimétrica (60% Principal / 40% Lateral) e Dock de Navegação.
3. **Camada de Serviços & Motores de Integração (Background Daemons):** Serviços Android em background para telemetria (AIDL), áudio (`MediaSession`), visão computacional (ADAS), inteligência de combustível e voz (Hermes).

```text
┌────────────────────────────────────────────────────────────────────────┐
│                   INTERFACE DE USUÁRIO (COMPOSE UI)                    │
│ ┌───────────────────────────┬────────────────────────────────────────┐ │
│ │                           │ Card 1: ADAS Vision HUD                │ │
│ │   ÁREA PRINCIPAL (60%)    ├────────────────────────────────────────┤ │
│ │  - Waze / Maps            │ Card 2: Player Mídia (Spotify/Finamp)  │ │
│ │  - Spark Player (IPTV)    ├────────────────────────────────────────┤ │
│ │                           │ Card 3: Telemetria HR-V (CVT / PIDs)   │ │
│ └───────────────────────────┴────────────────────────────────────────┘ │
│ └────── DOCK ERGONÔMICO: [Home] [Waze] [Mídia] [TV] [meucarro] [Hermes] ──┘ │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ StateFlow / Coroutines
┌────────────────────────────────────▼───────────────────────────────────┐
│                    CAMADA DE SERVIÇOS & INTEGRAÇÕES                    │
│ ┌───────────────────────────┐ ┌──────────────────────────────────────┐ │
│ │   TorqueAidlService       │ │      MediaSessionBridge              │ │
│ │   - IPC AIDL Torque Pro   │ │      - Spotify & Finamp (Jellyfin)   │ │
│ │   - PIDs: CVT, Motor, Gas │ │      - Spark Player Audio Control    │ │
│ └─────────────┬─────────────┘ └──────────────────┬───────────────────┘ │
│               │                                  │                     │
│ ┌─────────────▼─────────────┐ ┌──────────────────▼───────────────────┐ │
│ │   FuelInsightEngine       │ │      HermesVoiceClient               │ │
│ │   - Gatilho Tanque <= 50% │ │      - Check-in de Inicialização     │ │
│ │   - API Preço da Hora BA  │ │      - Alertas de CVT / Combustível  │ │
│ │   - Matching Rota Waze    │ │      - Home Assistant (Portão/AC)    │ │
│ └─────────────┬─────────────┘ └──────────────────┬───────────────────┘ │
│               │                                  │                     │
│ ┌─────────────▼─────────────┐ ┌──────────────────▼───────────────────┐ │
│ │   MeuCarroSyncService     │ │      BootCustomizer (MULTIMIDIA)     │ │
│ │   - Alertas Preventiva    │ │      - Logo Splash 1280x720          │ │
│ │   - Histórico de Consumo  │ │      - Bootanimation Blueprint HR-V  │ │
│ └───────────────────────────┘ └──────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Especificação Detalhada dos Componentes

### 🟡 COMPONENTE 1: `BootCustomizer` (`douglasricardos/MULTIMIDIA-ANDROID`)
* **Papel:** Substituir as imagens e animações de boot genéricas da K4811 pela identidade visual do Honda HR-V 2017.
* **Artefatos:**
  - `logo.bin` / `splash.img`: Imagem estática 1280x720 com o emblema Honda e badge HR-V.
  - `bootanimation.zip`: Sequência de frames em formato wireframe 3D blueprint do HR-V com renderização suave a 30 FPS.
* **Mecanismo de Instalação:** Script shell executado via Termux com permissões root ou ADB local Wi-Fi, sobrescrevendo `/system/media/bootanimation.zip` após criação de backup.

### 🟡 COMPONENTE 2: `TorqueAidlService` (Telemetria ECU & Câmbio CVT)
* **Papel:** Consumir em tempo real a telemetria do veículo sem implementar drivers seriais Bluetooth instáveis.
* **Interface IPC:** `org.prowl.torque.remote.ITorqueService`.
* **PIDs Mapeados para o Honda HR-V:**
  - `010D`: Velocidade Real (km/h).
  - `010C`: Rotação do Motor (RPM).
  - `0105`: Temperatura do Líquido de Arrefecimento (°C) [Alerta > 102°C].
  - `221940`: Temperatura do Fluido de Transmissão Honda CVT HCF-2 (°C) [Alerta > 95°C].
  - `012F`: Nível de Combustível (%) [Gatilho em ≤ 50%].
* **Frequência de Polling:**
  - PIDs dinâmicos (RPM, Velocidade): a cada 250ms.
  - PIDs de temperatura e combustível: a cada 2000ms.

### 🟡 COMPONENTE 3: `MediaSessionBridge` (Spotify + Finamp/Jellyfin + Spark TV)
* **Papel:** Centralizar metadados e comandos de transporte de mídia em um único card reativo.
* **Mecanismo:**
  - Instancia `MediaSessionManager.OnActiveSessionsChangedListener`.
  - Captura sessões ativas com filtro prioritário:
    * Se pacote for `org.schabi.finamp`: consome metadados e capas da biblioteca Jellyfin.
    * Se pacote for `com.spotify.music`: consome Web API / MediaSession nativo.
    * Se pacote for `Spark Player (IPTV)`: o card de mídia exibe "Modo TV Ativo" e cede foco de áudio.

### 🟡 COMPONENTE 4: `FuelInsightEngine` (Preço da Hora Bahia + Waze)
* **Papel:** Monitoramento proativo de economia de combustível em trânsito.
* **Gatilho de Execução:** Nível de combustível ≤ 50%.
* **Fluxo de Dados:**
  1. Captura localização atual (Latitude, Longitude) e vetor de movimento.
  2. Executa requisição na API Preço da Hora Bahia (`https://api.precodahora.ba.gov.br/v1/combustiveis`) via client HTTP seguro.
  3. Parâmetros de Busca:
     - `termo`: "gasolina" ou "etanol" (ou código ANP correspondente).
     - `raio`: 10 km.
     - `horas`: 48 (somente NFs recentes).
     - `ordenar`: "preco.asc".
  4. Valida se o posto com menor preço está alinhado à rota e dispara evento com `nomeEstabelecimento`, `precoUnitario` e `distancia`.

### 🟡 COMPONENTE 5: `HermesVoiceClient` (Copiloto Conversacional & Home Assistant)
* **Papel:** Assistente por voz embarcado com capacidades proativas e reativas.
* **Comunicação:** WebSocket seguro com o gateway central Hermes (`omniroute.drscloud.com.br`).
* **Casos de Uso de Voz:**
  - **Check-in de Partida:** Assim que a ECU conecta, realiza *audio ducking* no Spotify/Finamp e narra o status geral:
    `"Sistemas do HR-V operacionais. Tanque em X%, temperatura do motor em Y°C e fluido CVT em Z°C..."`
  - **Alerta de Combustível (≤ 50%):** Projeta fala sugerindo o posto mais econômico no trajeto.
  - **Alerta de Temperatura Crítica:** Alerta imediato se o fluido CVT HCF-2 exceder 95°C.
  - **Comandos de Voz do Volante (SWC):** Aciona automações do Home Assistant (abrir portão, ligar ar condicionado) ou faz consultas à base do `meucarro`.

### 🟡 COMPONENTE 6: `MeuCarroSyncService` (Gestão Preventiva HR-V)
* **Papel:** Integrar com o ecossistema `douglasricardos/meucarro`.
* **Regras de Negócio:**
  - Monitorar odômetro atual vs. prazos de troca:
    * Óleo de motor 0W-20 sintético (a cada 10.000 km ou 12 meses).
    * Fluido de transmissão CVT Honda HCF-2 (a cada 40.000 km).
    * Fluido de freio DOT 4 (a cada 2 anos).
  - Ao desligar a ignição, computa e envia telemetria resumida da viagem (KM rodados, consumo médio, pico de temperatura) para o banco do `meucarro`.

---

## 3. Matriz de Edge Cases & Resiliência
* 🟡 **Falha de Pareamento ELM327 Bluetooth:** Se o dongle desconectar, o card de telemetria exibe "Reconectando ECU..." sem travar o launcher; o card de mídia e o Waze continuam operando normalmente.
* 🟡 **Sem Conexão de Internet (Túnel / Sem Sinal 4G):** O `FuelInsightEngine` retém a última consulta de preços em cache e suspende novas requisições até o restabelecimento da conectividade.
* 🟡 **Conflito de Foco de Áudio:** Se o Spark Player for aberto em tela cheia, o launcher pausa automaticamente o Spotify/Finamp para evitar sobreposição de áudio nos alto-falantes do HR-V.
