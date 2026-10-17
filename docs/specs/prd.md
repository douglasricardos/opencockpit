# 📋 PRD — Product Requirements Document
**Projeto:** `OpenCockpit` / `meucarro-cockpit` (BYD & GWM Inspired Head Unit Ecosystem)  
**Alvo de Hardware:** Central Multimídia K4811 NWD (9", 1280x720, Android real, 8 GB RAM)  
**Veículo:** Honda HR-V EXL 2017 (Câmbio CVT Honda HCF-2)  
**Versão:** 1.1.0-final  
**Autor:** Douglas Ricardo & Hermes Agent  

---

### 1. Visão do Produto & Proposta de Valor
Transformar a central multimídia K4811 do Honda HR-V 2017 em uma experiência veicular premium, moderna e preditiva. O sistema substitui o visual arcaico dos launchers genéricos por um design cockpit inspirado nas centrais **BYD (DiLink)** e **GWM (Coffee OS)**, integrando desde o boot customizado (blueprint HR-V) até a telemetria profunda da ECU/CVT, entretenimento multimídia contínuo, copiloto de voz inteligente Hermes, segurança ativa por visão computacional (ADAS) e monitoramento de combustível em tempo real via SEFAZ-BA.

---

### 2. Personas & Jornadas do Usuário

#### Jornada 1: Partida & "Check-in do Cockpit" (Boot & Saída)
1. Ao acionar a ignição, a tela exibe a splash screen oficial Honda HR-V seguida da `bootanimation` personalizada com o blueprint wireframe do HR-V 2017 acendendo os faróis em 1280x720 (base `douglasricardos/MULTIMIDIA-ANDROID`).
2. O launcher inicia instantaneamente em segundo plano e conecta via AIDL ao daemon do Torque Pro.
3. Se a chave estiver conectada e os sensores estáveis, o áudio da mídia é atenuado (*ducking*) e o **Hermes Voice** emite o briefing matinal/inicial:
   > *"Sistemas do HR-V operacionais. Combustível em 65%, temperatura do motor em 86°C e fluido CVT em 70°C. Atenção: revisão de óleo de câmbio HCF-2 em 1.500 km pelo meucarro. Boa viagem!"*

#### Jornada 2: Condução Urbana & Rodoviária com Copiloto Preditivo
1. **Layout Principal (60% da tela):** Navegação ativa (Waze ou Google Maps) com visão ampla de tráfego.
2. **Cards Laterais (40% da tela):**
   * **Card 1 (Superior):** Feed e alertas da câmera ADAS (saída de faixa LDW e distância frontal FCW).
   * **Card 2 (Central):** Player de mídia unificado (Spotify ou Finamp/Jellyfin) com arte do álbum e controle direto.
   * **Card 3 (Inferior):** Telemetria em tempo real (Velocidade, RPM, Temp Motor e Temp Fluido CVT HCF-2 com alerta visual se > 95°C).
3. **Alerta de Combustível Oportuno (Gatilho em 50%):**
   * Assim que o nível de combustível cai para **≤ 50%**, o `FuelInsightEngine` ativa o rastreamento em rota.
   * Ao traçar trajeto no Waze, o launcher varre a **API Preço da Hora Bahia (SEFAZ-BA)** em um polígono de 5–10 km na direção do deslocamento.
   * Ao detectar um posto com preço abaixo da média com NF emitida nas últimas 24h, o Hermes fala por voz e projeta um card com a economia estimada e opção de adicionar parada com 1 toque.

#### Jornada 3: Parada, Espera & TV a Bordo
1. Com o veículo parado/estacionado, o motorista toca no ícone de TV no Dock inferior.
2. O **IPTV Spark Player** assume a área principal (ou tela cheia).
3. Ao retomar a marcha ou tocar nas bordas, o layout do cockpit é restaurado com suavidade.

#### Jornada 4: Chegada em Casa & Integração Residencial
1. Ao aproximar-se da residência (Geofence GPS < 500m), o launcher dispara via WebSocket para o **Home Assistant / Hermes**:
   * Abertura antecipada de portão eletrônico.
   * Acionamento de luzes externas e climatização.

---

### 3. Requisitos Funcionais (RF) com Selo 🟡

* 🟡 **RF-01 (Boot Experience HR-V):** Inicialização customizada com splash screen 1280x720 e animação de blueprint vetorial do HR-V 2017 substituindo os arquivos padrão via scripts do repositório `douglasricardos/MULTIMIDIA-ANDROID`.
* 🟡 **RF-02 (Layout Assimétrico BYD/GWM):** Tela dividida em proporção 60/40 em 1280x720 landscape:
  - 60% Área de Navegação/Vídeo (Waze, Maps ou Spark Player).
  - 40% Coluna de 3 Cards Flutuantes (ADAS, Mídia, Telemetria).
  - Dock inferior fixo com atalhos táteis ergonômicos (Home, Waze, Mídia, Spark TV, meucarro, Hermes/HA, Apps, Configurações).
* 🟡 **RF-03 (Telemetria ECU & Câmbio CVT):** Conexão via Android AIDL (`ITorqueService`) com o daemon Torque Pro, recebendo os PIDs:
  - `010D` (Velocidade km/h)
  - `010C` (RPM)
  - `0105` (Temperatura Arrefecimento do Motor)
  - `012F` (Nível do Tanque de Combustível %)
  - `221940` (Temperatura Fluido de Transmissão Honda CVT HCF-2)
* 🟡 **RF-04 (Briefing de Voz "Check-in do Cockpit"):** Ao sincronizar a telemetria após partida, o Hermes Agent gera áudio TTS com status de temperatura, nível de combustível e pendências do veículo.
* 🟡 **RF-05 (Gatilho Inteligente de Combustível a 50%):** Quando o PID `012F` registrar ≤ 50%:
  - O sistema monitora a rota ativa.
  - Consulta a API Preço da Hora Bahia (`precodahora-ba`) para Gasolina e Etanol por código ANP.
  - Projeta alerta visual e fala via Hermes sugerindo paradas de menor preço com NF recente.
* 🟡 **RF-06 (Gestão de Mídia Híbrida):** Suporte nativo a **Spotify** e **Finamp** (Jellyfin) através do `MediaSessionManager` do Android, permitindo controle de faixa, play/pause e exibição de metadados em tempo real.
* 🟡 **RF-07 (Integração IPTV Spark Player):** Suporte a lançamento do Spark Player em container prioritário de vídeo com restauração de layout sem travamentos.
* 🟡 **RF-08 (Sincronização com `meucarro`):** Consulta a manutenções preventivas do Honda HR-V (óleo motor 0W-20, óleo CVT HCF-2, filtros, fluido de freio) e envio de estatísticas de bordo ao término da corrida.
* 🟡 **RF-09 (Card Overlay de ADAS):** Integração com o feed da câmera USB e exibição de alertas de faixa (LDW) e colisão (FCW) com prioridade visual em caso de evento crítico.
* 🟡 **RF-10 (Comandos por Voz e Home Assistant):** Recepção de comandos por toque no dock ou botão de volante (SWC), roteando intenções locais do carro e automações residenciais pelo ecossistema Hermes.

---

### 4. Requisitos Não-Funcionais (RNF)
* 🟡 **RNF-01 (Estabilidade em Hardware Automotivo):** Consumo de memória RAM do Launcher < 220 MB para garantir fluidez multitarefa na K4811.
* 🟡 **RNF-02 (Isolamento de Falhas):** Se a conexão Bluetooth do ELM327 ou a API da SEFAZ oscilar, o Launcher mantém Waze e áudio rodando a 60 FPS sem congelamentos na UI.
* 🟡 **RNF-03 (Zero Secret Exposure):** Nenhuma chave de API, credencial ou token de serviço trafega ou é gravado em texto aberto no APK; conexões externas autenticam via tokens assinados pelo cofre DRSCloud.
