# Trade Republic PDF Downloader
This project was created out of the need for a bulk download option of **Sparplan**, **Dividenden** and **Zinsen** PDFs from Trade Republic.</br>
And also to practice some Kotlin...

## Prerequisites
You need JRE 11 or higher installed.

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