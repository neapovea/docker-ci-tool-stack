# mamohr/subversion-edge

This is a docker image of the Collabnet Subversion Edge Server

## Usage

The image is exposing the data dir of csvn as a volume under `/opt/csvn/data`.
If you provide an empty host folder as volume the init scripts will take care of copying a basic configuration to the volume.
The container exposes the following ports:

 * 3343 - HTTP CSVN Admin Sites
 * 4434 - HTTPS CSVN Admin Sites (If SSL is enabled)
 * 18080 - Apache Http SVN

The simplest way to start a subversion edge server is

    docker run -d mamohr/subversion-edge

This will run the server. It will only be reachable from the docker host by using the container ip address

Exposing the ports from the host:
    
    docker run -d -p 3343:3343 -p 4434:4434 -p 18080:18080 \
        --name svn-server mamohr/subversion-edge

This will make the admin interface reachable under [http://docker-host:3343/csvn](http://docker-host:3343/csvn).

If you want to provide a host path for the data use command like this:

    docker run -d -p 3343:3343 -p 4434:4434 -p 18080:18080 \
        -v /srv/svn-data:/opt/csvn/data --name svn-server mamohr/subversion-edge
    

For information to further configuration please consult the documentation at [CollabNet](http://collab.net/products/subversion).



Sample to Load test subversion repo

Let's load a test SVN repository, located here.

    Dump SVN repo to local Docker container

    $ docker exec -it svn-server sh -c "svnrdump dump https://svn.code.sf.net/p/ultrastardx/svn | gzip > /tmp/ultrastardx.dump.gz"

    Create new repo to host code

    $ docker exec -it svn-server svnadmin create ultrastardx

    Load in the SVN dump archive (make sure your dashes and quotes aren't funky here)

    $ docker exec -it svn-server sh -c "gunzip -c /tmp/ultrastardx.dump.gz | svnadmin load ultrastardx"

    Check to see that repo has been loaded properly

    $ svn info svn://localhost:3960/ultrastardx

sudo docker exec -it docker-ci-tool-stack_subversion_1 sh -c " /opt/csvn/bin/svnrdump dump  https://github.com/neapovea/docker-ci-tool-stack/tree/master/repoSVN/jenkins --trust-server-cert --non-interactive| gzip > /tmp/svndumpjenkins.dump.gz"

docker exec -it docker-ci-tool-stack_subversion_1 sh /opt/csvn/bin/svnadmin create jenkins

docker exec -it docker-ci-tool-stack_subversion_1 sh sh -c "gunzip -c /tmp/svndumpjenkins.dump.gz | /opt/csvn/bin/svnadmin load jenkins"

docker exec -it docker-ci-tool-stack_subversion_1 sh /opt/csvn/bin/svn info svn://172.19.0.3:3960/jenkins


https://github.com/neapovea/docker-ci-tool-stack/tree/master/repoSVN
https://github.com/neapovea/docker-ci-tool-stack/tree/master/repoSVN/prueba_maven



gzip -cr repoSVN/jenkins/ > jenkins.gz
gzip -cr repoSVN/prueba_maven/ > prueba_maven.gz

sudo docker cp jenkins.gz docker-ci-tool-stack_subversion_1:/home/jenkins.gz
sudo docker cp prueba_maven.gz docker-ci-tool-stack_subversion_1:/home/prueba_maven.gz


docker exec -it docker-ci-tool-stack_subversion_1 sh /opt/csvn/bin/svnadmin create jenkins


docker exec -it docker-ci-tool-stack_subversion_1 sh -c "gunzip -c /home/jenkins.gz | /opt/csvn/bin/svnadmin load jenkins"

docker exec -it docker-ci-tool-stack_subversion_1 sh /opt/csvn/bin/svn info svn://172.19.0.3:3960/jenkins



sudo docker cp repoSVN/jenkins docker-ci-tool-stack_subversion_1:/home/

sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svnadmin create repo
sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn mkdir /opt/csvn/bin/svn/repo/jenkins
sudo docker exec -it docker-ci-tool-stack_subversion_1  cd repo
sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn checkout repo/jenkins /home/jenkins"
sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn add /home/jenkins/*
sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn commit /home/jenkins /path/to/codebase -m "inicio repo jenkins"




svn mkdir <repo>/newProject
svn checkout <repo>/newProject /path/to/codebase
svn add /path/to/codebase/*
svn commit /path/to/codebase -m "adding initial codebase"


svnadmin create /path/to/<repository_name>


sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn mkdir --username admin --password admin  http://localhost:18080/svn/jenkins -m "Creating jenkins"


sudo docker exec -it docker-ci-tool-stack_subversion_1  /opt/csvn/bin/svn mkdir /opt/csvn/repo/jenkins

svn checkout --username admin --password admin  http://localhost:18080/svn/jenkins /home/alx/Documentos/DockerCI/docker-ci-tool-stack/repoSVN/jenkins