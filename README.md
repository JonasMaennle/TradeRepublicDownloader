# Trade Republic PDF Downloader
This project was created out of the need for a bulk download option of **Sparplan**, **Dividenden** and **Zinsen** PDFs from Trade Republic.</br>
And also to practice some Kotlin...

## Disclaimer
This is a private project. It is **not** supported or associated by https://traderepublic.com

## Prerequisites
- You need ```JRE 11``` or higher installed to run the application
- Build requires ```maven 3.x.x```

## Limitations
This tool just works for **de** locale.</br>

## Run the Application
Build project with Maven:

```shell
mvn clean install
```

Start the application with the generated .exe in ```./target```

Or start the commandline tool as jar:
```shell
java -jar .\target\TradeRepublicDownloader-1.0-jar-with-dependencies.jar
```