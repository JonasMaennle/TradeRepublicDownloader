# Trade Republic PDF Downloader
This project was created out of the need for a bulk download option of *Sparplan* PDFs.</br>
And also to practice some Kotlin...

## Prerequisites
You need JRE 11 or higher installed.

## Limitations
The tool just works at the moment for **de** locale.</br>
The tool will only download the *Sparplan* files of the current month.

## Run the Application
Build project with Maven:

```shell
mvn clean install
```

Start the commandline tool:
```shell
java -jar .\target\TradeRepublicDownloader-1.0-jar-with-dependencies.jar
```

