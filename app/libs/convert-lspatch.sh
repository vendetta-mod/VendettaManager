# Download LSPatch jar and convert it to an aar
# Jars cannot be used as a dependency without breaking R8 optimization

# TODO: update to latest LSPatch
#LSPATCH_URL="https://github.com/LSPosed/LSPatch/releases/download/v0.6/jar-v0.6-398-release.jar"

LSPATCH_URL="https://github.com/vendetta-mod/VendettaManager/raw/5764b16a14c42d8449722f3656b2cb42019b82a8/app/libs/lspatch.jar"
LSPATCH_FILE_NAME="lspatch"

function cleanup {
  rm -rf $LSPATCH_FILE_NAME.jar META-INF AndroidManifest.xml classes.jar R.txt
}

cleanup

# download lspatch
curl -sSL -o $LSPATCH_FILE_NAME.jar $LSPATCH_URL

# prepare aar contents
unzip -q $LSPATCH_FILE_NAME.jar "META-INF/**"
mv $LSPATCH_FILE_NAME.jar classes.jar
touch R.txt
cat >AndroidManifest.xml <<EOF
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="org.lsposed.lspatch">
    <uses-sdk android:minSdkVersion="28" android:targetSdkVersion="34" />
</manifest>
EOF

# add everything to aar
rm -f $LSPATCH_FILE_NAME.aar
zip -rq $LSPATCH_FILE_NAME.aar R.txt AndroidManifest.xml META-INF classes.jar
cleanup
