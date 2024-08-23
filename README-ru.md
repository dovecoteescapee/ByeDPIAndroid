# ByeDPI for Android

[English](README.md) | **Русский**

<div style="text-align: center;">
  <img alt="Логотип ByeDPI" src=".github/images/logo.svg" width="100%" height="200px">
</div>

---

Приложение для Android, которое запускает локальный VPN-сервис для обхода DPI (Deep Packet Inspection) и цензуры.

Приложение локально запускает SOCKS5-прокси [ByeDPI](https://github.com/hufrea/byedpi) и перенаправляет весь трафик через него.

## Установка

[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Скачать с GitHub"
    height="80">](https://github.com/dovecoteescapee/ByeDPIAndroid/releases)
[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
alt="Скачать с IzzyOnDroid"
height="80">](https://apt.izzysoft.de/fdroid/index/apk/io.github.dovecoteescapee.byedpi)

### Или используйте Obtainium

1. Установите [Obtainium](https://github.com/ImranR98/Obtainium/blob/main/README.md#installation)
2. Добавьте приложение по URL:  
   `https://github.com/dovecoteescapee/ByeDPIAndroid`

## Настройки

Для обхода некоторых блокировок может потребоваться изменить настройки. Подробнее о различных настройках можно прочитать в [документации ByeDPI](https://github.com/hufrea/byedpi/blob/v0.13/README.md).

## FAQ

### У меня не получается настроить. Что делать?

Вы можете попросить помощи в [discussion](https://github.com/dovecoteescapee/ByeDPIAndroid/discussions).

### Приложение требует root-права?

Нет. Все функции приложения работают без root-прав.

### Это VPN?

Нет. Приложение использует VPN-режим на Android для перенаправления трафика, но не передает ничего на удаленный сервер. Оно не шифрует трафик и не скрывает ваш IP-адрес.

### Какие данные собирает приложение?

Никакие. Приложения не отправляет никакие данные на удаленный сервер. Весь трафик обрабатывается на устройстве.

### Как использовать ByeDPI вместе с AdGuard?

1. Запустите ByeDPI в режиме прокси.
2. Добавьте ByeDPI в исключения AdGuard на вкладке "Управление приложениями".
3. В настройках AdGuard укажите прокси:

   ```plaintext
   Тип прокси: SOCKS5
   Хост: 127.0.0.1
   Порт: 1080 (по умолчанию)
   ```

### А есть для других платформ?

[Список аналогов](https://github.com/ValdikSS/GoodbyeDPI/blob/master/README.md#similar-projects)

### Что такое DPI?

DPI (Deep Packet Inspection) - это технология для анализа и фильтрации трафика. Она используется провайдерами и государственными органами для блокировки сайтов и сервисов.

## Зависимости

- [ByeDPI](https://github.com/hufrea/byedpi)
- [hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel)

## Сборка

Для сборки приложения требуется:

1. JDK 8 или новее
2. Android SDK
3. Android NDK
4. CMake 3.22.1 или новее

Сборка приложения:

1. Клонируйте репозиторий с подмодулями:
   ```bash 
   git clone --recurse-submodules
   ```
2. Запустите скрипт сборки из корня репозитория:
   ```bash
   ./gradlew assembleRelease`
   ```
3. APK будет лежать в `app/build/outputs/apk/release/`
