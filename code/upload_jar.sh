PACKAGE_NAME="cs224u-disentanglement-1.0-SNAPSHOT-jar-with-dependencies.jar"
BASE_DIRECTORY="~/projects/cs224u"

# Build package
echo "Building package..."
mvn package

# Upload package
echo "Uploading package..."
rsync -ae ssh "target/$PACKAGE_NAME" "corn:$BASE_DIRECTORY"
