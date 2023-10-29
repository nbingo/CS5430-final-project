if [ -d "./bin/" ] 
then
  rm -r bin/
fi
if [ -f "phase1_impl.jar" ]
then
  rm phase1_impl.jar
fi
if [ -f "phase2_impl.jar" ]
then
  rm phase2_impl.jar
fi
