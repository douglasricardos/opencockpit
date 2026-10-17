#!/bin/bash
# OpenCockpit - Fast ADB Bootanimation Deployer over Wi-Fi / USB
# Usage: ./adb_flash_bootanimation.sh [IP_MULTIMIDIA]

TARGET_IP=${1:-"192.168.1.150"}
PORT=5555

echo "=== [OpenCockpit] Flash de Bootanimation via ADB ==="

if [ -n "$1" ]; then
  echo "Conectando a $TARGET_IP:$PORT..."
  adb connect ${TARGET_IP}:${PORT}
fi

echo "Verificando dispositivos conectados..."
adb devices

echo "Enviando bootanimation.zip para a multimidia..."
adb push bootanimation.zip /data/local/tmp/bootanimation.zip

echo "Executando instalacao com privilegios root..."
adb shell "su -c 'mount -o rw,remount /system 2>/dev/null; mount -o rw,remount / 2>/dev/null; cp /data/local/tmp/bootanimation.zip /data/local/bootanimation.zip; chmod 644 /data/local/bootanimation.zip; cp /data/local/tmp/bootanimation.zip /system/media/bootanimation.zip 2>/dev/null; chmod 644 /system/media/bootanimation.zip 2>/dev/null; echo Bootanimation instalada com sucesso!'"

echo "Deseja reiniciar a multimidia agora para testar? (s/n)"
read -r RESP
if [ "$RESP" = "s" ] || [ "$RESP" = "S" ]; then
  echo "Reiniciando multimidia..."
  adb reboot
fi
