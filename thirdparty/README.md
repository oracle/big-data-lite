# Third party software management scripts

This folder contains scripts useful to easily install and start / stop third party software that is useful to play some of our demos.

## Zeppelin Notebooks with the PGX interpreter
Just run:
```bash
./zeppelin.sh install
./zeppelin.sh start
./zeppelin.sh stop
```
to install, start, and stop the service, respectively.

## Jupyter (iPython) Notebooks
Just run:
```bash
./jupyter.sh install
./jupyter.sh start
./jupyter.sh stop
```
to install, start, and stop the service, respectively.

## RStudio
Just run:
```bash
./rstudio.sh install
./rstudio.sh start
./rstudio.sh stop
```
to install, start, and stop the service, respectively.

## Adding new scripts
Scripts are expected to take three commands: install, start, and stop.
Intuitively, install should get the third party software from the internet and install it locally into the inst directory.
Scripts need to be good citizens and make sure they do not overwrite somebody else's files / directories.
The start and stop commands should start / stop the third party app / service.
You can use thirdparty/example.sh as a skeleton for your script.

