#!/bin/sh
# mac
echo "======= Packing for mac x86_64 ======="
java -jar packr.jar \
     -platform mac \
     -jdk "../openjdk-1.7.0-u45-unofficial-icedtea-2.4.3-macosx-x86_64-image.zip" \
     -executable endest_of_days \
     -appjar desktop/target/hack-0.1.jar \
     -mainclass "hack/Main" \
     -vmargs "-Xmx1G" \
     -minimizejre "soft" \
     -outdir endest_of_days.app
zip -r endest_of_days.mac.zip endest_of_days.app
echo "======= Packing for windows i586 ======="
java -jar packr.jar \
     -platform windows \
     -jdk "../openjdk-1.7.0-u45-unofficial-icedtea-2.4.3-windows-i586-image.zip" \
     -executable endest_of_days \
     -appjar desktop/target/hack-0.1.jar \
     -mainclass "hack/Main" \
     -vmargs "-Xmx1G" \
     -minimizejre "soft" \
     -outdir endest_of_days
zip -r endest_of_days.win.zip endest_of_days
#      -jdk "../openjdk-1.7.0-u60-unofficial-macosx-x86_64-image.zip" \
#      -jdk "../openjdk-1.7.0-u45-unofficial-icedtea-2.4.3-macosx-x86_64-image.zip" \
#     -resources pom.xml;src/main/resources \