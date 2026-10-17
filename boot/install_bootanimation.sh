#!/bin/sh
# OpenCockpit - K4811 Bootanimation Installer (Termux / ADB / Root Shell)
# Target: NWD K4811 / AC8257 (1280x720) - Honda HR-V 2017

echo "=== [OpenCockpit] Instalador de Bootanimation HR-V (1280x720) ==="

BOOT_ZIP="/data/local/bootanimation.zip"
SYS_BOOT_ZIP="/system/media/bootanimation.zip"

if [ ! -f "bootanimation.zip" ]; then
  echo "[ERRO] Arquivo bootanimation.zip nao encontrado no diretorio atual."
  exit 1
fi

echo "[1/3] Remontando particoes em modo leitura/escrita..."
mount -o rw,remount / 2>/dev/null
mount -o rw,remount /system 2>/dev/null
mount -o rw,remount /vendor 2>/dev/null

echo "[2/3] Instalando bootanimation em /data/local e /system/media..."
cp -f bootanimation.zip $BOOT_ZIP
chmod 644 $BOOT_ZIP
chown root:root $BOOT_ZIP 2>/dev/null

if [ -w "/system/media" ]; then
  if [ -f "$SYS_BOOT_ZIP" ] && [ ! -f "${SYS_BOOT_ZIP}.bak" ]; then
    echo "Fazendo backup da bootanimation original..."
    cp $SYS_BOOT_ZIP "${SYS_BOOT_ZIP}.bak"
  fi
  cp -f bootanimation.zip $SYS_BOOT_ZIP
  chmod 644 $SYS_BOOT_ZIP
  chown root:root $SYS_BOOT_ZIP 2>/dev/null
  echo "[OK] Instalado em /system/media/bootanimation.zip"
fi

echo "[3/3] Validando permissoes e arquivo..."
ls -la $BOOT_ZIP

echo "=== Instalacao Concluida! Reinicie a multimidia com: reboot ==="
