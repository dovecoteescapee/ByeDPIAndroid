# ByeDPI for Android

[English](README.md) | **Русский**

<div align="center">
<img src=".github/images/logo.svg" height="200px" width="200px" />
</div>

---

Приложение для Android, которое запускает локальный VPN-сервис для обхода DPI (Deep Packet Inspection) и цензуры.

Приложение локально запускает [ByeDPI](https://github.com/hufrea/byedpi) и перенаправляет весь TCP трафик через него.

## Как использовать?

1. Скачать .apk файл из [релизов](https://github.com/dovecoteescapee/ByeDPIAndroid/releases/latest)
2. Установить
3. Запустить и нажать Connect 

## Настройки

Для обхода некоторых блокировок может потребоваться изменить настройки. Подробнее о различных настройках можно прочитать в [документации ByeDPI](https://github.com/hufrea/byedpi#readme-ov-file).

## FAQ

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
- [Tun2Socks](https://github.com/dovecoteescapee/tun2socks)*  
  *форк с добавление раздельного тунелирования TCP и UDP
