#
# Latest R, RServer Dockerfile
#
# build image with		docker build RServer/ -t latest_r
# start R with			docker run --name latest_r -p 6311:6311 -itd latest_r
#
# then you'll have a local port 6311 to access from any language to a Rserve
# WARNING: do not expose the port without adding some security layer!!!
###############################################################################

# Pull base image.
FROM debian:latest

MAINTAINER Nelson Gomes "nelson.ricardo.gomes@gmail.com"

RUN apt-get update
RUN apt-get install -y software-properties-common net-tools vim

RUN gpg --keyserver pgpkeys.mit.edu --recv-key 06F90DE5381BA480
RUN gpg -a --export 06F90DE5381BA480 | apt-key add -
RUN add-apt-repository 'deb http://cran-mirror.cs.uu.nl/bin/linux/debian jessie-cran3/'

RUN apt-get update
RUN apt-get install -y r-base-core r-cran-rserve 

# Define working directory.
WORKDIR /data

# create RServe initial config
RUN echo 'remote enable\nworkdir /data/\n' > /etc/Rserv.conf

#R CMD Rserve --help
#ENV R_HOME=/usr/lib/R
#RUN /usr/lib/R/bin/Rserve

# create command to launch Rserve
RUN echo '#!/bin/bash\n/usr/bin/R CMD Rserve --no-save\ntail -f /dev/null' > /data/start.sh
RUN chmod +x /data/start.sh

# Define default command.
CMD ["/data/start.sh"]
