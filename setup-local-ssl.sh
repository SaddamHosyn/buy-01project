#!/usr/bin/env bash
set -euo pipefail

# Helper to generate a locally-trusted TLS certificate for localhost using mkcert
# Produces: ./certs/localhost.pem, ./certs/localhost-key.pem, ./certs/keystore.p12
# Keystore password: changeit (used by docker-compose env)

CERT_DIR="./certs"
KEYSTORE_PASS="changeit"

mkdir -p "$CERT_DIR"

if ! command -v mkcert >/dev/null 2>&1; then
  echo "mkcert not found. Installing via Homebrew..."
  if command -v brew >/dev/null 2>&1; then
    brew install mkcert
    brew install nss || true
    mkcert -install
  else
    echo "Please install mkcert manually: https://github.com/FiloSottile/mkcert"
    exit 1
  fi
fi

echo "Generating certificates for: localhost, 127.0.0.1, ::1"
mkcert -cert-file "$CERT_DIR/localhost.pem" -key-file "$CERT_DIR/localhost-key.pem" localhost 127.0.0.1 ::1

echo "Creating PKCS12 keystore at $CERT_DIR/keystore.p12"
openssl pkcs12 -export \
  -in "$CERT_DIR/localhost.pem" \
  -inkey "$CERT_DIR/localhost-key.pem" \
  -out "$CERT_DIR/keystore.p12" \
  -name tomcat \
  -passout pass:$KEYSTORE_PASS

chmod 640 "$CERT_DIR/keystore.p12"

echo "Done. Keystore: $CERT_DIR/keystore.p12 (password: $KEYSTORE_PASS)"
echo "IMPORTANT: Do not commit ./certs to git. Add it to .gitignore if needed."

echo "You can now run: docker-compose down && docker-compose up --build -d"
echo "Open https://localhost in your browser. The generated CA should be trusted by the OS/browser (mkcert installed it)."
