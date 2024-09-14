# Trade Republic PDF Downloader
This project was created out of the need for a bulk download option of **Sparplan**, **Dividenden**, **Zinsen** and **Order** PDFs from Trade Republic.</br>
And also to practice some Kotlin...

## Disclaimer
This is a private project. It is **not** supported or associated by https://traderepublic.com

## Prerequisites
- You need ```JDK 21``` or higher

## Limitations
This tool just works for **DE** locale.</br>

## Build & Run the Application
For login convenience you can set a .env file to the resources folder.
Just copy the **.env.example** remove the **.example** suffix and set your credentials like in the example below.
```
PHONE_NUMBER="+49123456789"
PIN="1234"
```

Build project with gradle:
```shell
.\gradlew build
```
Run Spring Boot app for development
```shell
.\gradlew bootRun
```

## Executable JAR & .exe
After running the **build** command you will find a jar with all dependencies in
the build folder
```shell
java -jar ./build/libs/'Trade Republic Downloader-1.0.0.jar'
```
You can also create a .exe (which still demands an installed JRE)

The 'Trade Republic Downloader.exe' will be generated in the build folder aswell.
```shell
.\gradlew createExe
```
