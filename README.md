# Project description

This project demonstrates communication via Bluetooth socket between Android device and Arduino.
It contains 2 projects:
- Android: contains gradle structured Android project
- Arduino: contains scatch for Arduino microcontroller

Required hardware:
1. Arduino Uno. The project was not tested on another types of microcontrollers except Arduino Uno.
2. Bluetooth module HC-05

The solutions works in the following way: Android devices searches for Bluetooth devices and connects to the selected.
Arduino microcontroller checks in loop serial port related with Bluetooth. When Android device sent something, Arduino will respond with predefined answer.


#License

[Apache Licence 2.0](http://www.apache.org/licenses/LICENSE-2.0)

Copyright 2015 Dmytro Khmelenko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
