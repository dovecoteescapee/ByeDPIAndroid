# ByeDPI for Android

[English](README.md) | **Русский**

<div style="text-align: center;">
  <img src=".github/images/logo.svg" width="100%" height="200px">
</div>

---

Приложение для Android, которое запускает локальный VPN-сервис для обхода DPI (Deep Packet Inspection) и цензуры.

Приложение локально запускает [ByeDPI](https://github.com/hufrea/byedpi) и перенаправляет весь трафик через него.

## Установка

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Скачать с GitHub"
    height="80">](https://github.com/dovecoteescapee/ByeDPIAndroid/releases)

### Или используйте Obtainium

1. Установите [Obtainium](https://github.com/ImranR98/Obtainium?tab=readme-ov-file#installation)
2. Добавьте приложение по URL:  
   `https://github.com/dovecoteescapee/ByeDPIAndroid`

## Настройки

Для обхода некоторых блокировок может потребоваться изменить настройки. Подробнее о различных настройках можно прочитать в [документации ByeDPI](https://github.com/hufrea/byedpi#readme-ov-file).

## FAQ

### Как получать обновления?

Используйте [Obtainium](#или-используйте-obtainium).

### Это VPN?

Нет. Приложение использует VPN-режим на Android для перенаправления трафика, но не передает ничего на удаленный сервер. Оно не шифрует трафик и не скрывает ваш IP-адрес.

### Какие данные собирает приложение?

Никакие. Приложения не отправляет никакие данные на удаленный сервер. Весь трафик обрабатывается на устройстве.

### А есть для других платформ?

Для Windows существует [GoodByeDPI](https://github.com/ValdikSS/GoodbyeDPI), а для Linux и macOS [zapret](https://github.com/bol-van/zapret). Также, вы можете использовать оригинальный [ByeDPI](https://github.com/hufrea/byedpi) для запуска аналогичного прокси на Windows, Linux или macOS.

[Другие аналоги](https://github.com/ValdikSS/GoodbyeDPI?tab=readme-ov-file#similar-projects)

### Что такое DPI?

DPI (Deep Packet Inspection) - это технология для анализа и фильтрации трафика. Она используется провайдерами и государственными органами для блокировки сайтов и сервисов. 

## Зависимости

- [ByeDPI](https://github.com/hufrea/byedpi)
- [Tun2Socks](https://github.com/xjasonlyu/tun2socks)

## Сборка

Для сборки приложения требуется:

1. JDK 8 или новее
2. Android SDK
3. Android NDK
4. Go 1.22 или новее
5. CMake 3.22.1 или новее

Сборка приложения:

1. Установите gomobile:
   ```bash
   go install golang.org/x/mobile/cmd/gomobile@latest
   gomobile init
   ```
2. Клонируйте репозиторий с подмодулями:
   ```bash 
   git clone --recurse-submodules
   ```
3. Запустите скрипт сборки из корня репозитория:
   ```bash
   ./gradlew assembleRelease`
   ```
4. APK будет лежать в `app/build/outputs/apk/release/`

## Если не работает из коробки

Попробуйте использовать Ютуб через браузер.

Или попробуйте сменить настройки в приложении на вот такие:

- DNS: 8.8.8.8
- Mode: VPN
- Desync only HTTPS and TLS: On
- Desync method: fake
- Split position: 1
- Split TLS record at SNI: On
