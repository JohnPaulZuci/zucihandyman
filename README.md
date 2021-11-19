# Handyman

## To get the repository clone

 > git clone https://github.com/zucisystems-dev/handyman.git

## To see the list of branch in the repo (default master)
 > git branch -a
 
## To change to the particular branch
 > git checkout {{branch}}
 
## To Clean, Compile and build, run the below maven commands 
  1. Inside handyman/handyman.parent  
     > mvn clean compile install package -Dmaven.test.skip=true
  2. Inside handyman/handyman.parent/handyman 
     > mvn clean compile install scala:compile install package -Dmaven.test.skip=true
    
 ### Generated jar file available in handyman/handyman.parent/handyman/target folder as *handyman.server-<version>-SNAPSHOT.jar*
