#!/usr/bin/env bash
# Build and publish a Nuvio + IPTV fork release without GitHub Actions.
# Requires: Java 17, Android SDK (sdk.dir in local.properties), signing + API keys
# in local.properties (same fields as .github/workflows/release-livetv-update.yml).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ ! -f local.properties ]]; then
  echo "Missing local.properties. Copy local.example.properties and add sdk.dir, NUVIO_SUPABASE_*, signing, and API keys." >&2
  exit 1
fi

if ! grep -q '^sdk\.dir=' local.properties; then
  echo "local.properties must set sdk.dir to your Android SDK." >&2
  exit 1
fi

BASE="$(grep -m1 -E '^\s*versionName = "' app/build.gradle.kts | sed -E 's/.*"(.*)".*/\1/')"
if [[ -z "$BASE" ]]; then
  echo "Could not read versionName from app/build.gradle.kts" >&2
  exit 1
fi

if [[ "$BASE" == *-* ]]; then
  PREFIX="${BASE}."
else
  IFS=. read -r MA MI PA <<< "$BASE"
  PREFIX="${MA}.${MI}.$((PA + 1))-livetv."
fi

LAST="$(
  gh release list -R "${GITHUB_REPOSITORY:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}" --limit 200 --json tagName -q '.[].tagName' \
    | sed "s/^v//" \
    | grep -F "$PREFIX" \
    | sed "s/^$(printf '%s' "$PREFIX" | sed 's/[.[\*^$/]/\\&/g')//" \
    | grep -E '^[0-9]+$' \
    | sort -n \
    | tail -1 \
    || true
)"
N=$(( ${LAST:-0} + 1 ))
if (( N > 99 )); then
  echo "Too many fork builds on one Nuvio version (max 99)" >&2
  exit 1
fi

VERSION="${PREFIX}${N}"
TAG="v${VERSION}"
NOTES="${1:-}"

echo "Nuvio base ${BASE} -> release ${VERSION} (livetv build ${N})"

if [[ ! -f local.dev.properties ]]; then
  cp local.properties local.dev.properties
fi

KEYSTORE="${NUVIO_RELEASE_STORE_FILE:-$ROOT/keystore/livetv-fork.jks}"
if [[ ! -f "$KEYSTORE" ]]; then
  echo "Release keystore not found: $KEYSTORE" >&2
  exit 1
fi

{
  grep -vE '^(NUVIO_RELEASE_STORE_FILE|NUVIO_RELEASE_KEY_ALIAS|NUVIO_RELEASE_KEY_PASSWORD|NUVIO_RELEASE_STORE_PASSWORD)=' local.properties || true
  echo "NUVIO_RELEASE_STORE_FILE=$KEYSTORE"
  echo "NUVIO_RELEASE_KEY_ALIAS=${NUVIO_RELEASE_KEY_ALIAS:-livetv}"
  echo "NUVIO_RELEASE_KEY_PASSWORD=${NUVIO_RELEASE_KEY_PASSWORD:-livetvfork}"
  echo "NUVIO_RELEASE_STORE_PASSWORD=${NUVIO_RELEASE_STORE_PASSWORD:-livetvfork}"
} > local.properties.tmp
mv local.properties.tmp local.properties

chmod +x gradlew
./gradlew :app:assembleFullRelease --stacktrace \
  -PlivetvBuildNumber="$N" \
  -PlivetvVersionName="$VERSION"

rm -rf out
mkdir -p out
for f in app/build/outputs/apk/full/release/*.apk; do
  name="$(basename "$f" .apk)"
  cp "$f" "out/NuvioPlusIPTV-${VERSION}-${name#app-full-}.apk"
done

REPO="${GITHUB_REPOSITORY:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"
if gh release view "$TAG" -R "$REPO" >/dev/null 2>&1; then
  echo "Release $TAG already exists on $REPO" >&2
  exit 1
fi

BODY="Nuvio + IPTV update ${VERSION}."
if [[ -n "$NOTES" ]]; then
  BODY="${BODY}

${NOTES}"
fi

gh release create "$TAG" -R "$REPO" \
  --target "$(git rev-parse HEAD)" \
  --title "Nuvio + IPTV ${VERSION}" \
  --prerelease \
  --notes "$BODY" \
  out/*.apk

APK="$(ls out/*universal*.apk 2>/dev/null | head -1)"
[[ -z "$APK" ]] && APK="$(ls out/*.apk | head -1)"
mkdir -p dl
cp "$APK" dl/NuvioPlusIPTV.apk
if ! gh release view downloader -R "$REPO" >/dev/null 2>&1; then
  gh release create downloader -R "$REPO" \
    --title "Nuvio + IPTV for Downloader (always the newest version)" \
    --notes "Replaced on each release so one Downloader link always installs the latest build." \
    --latest=false --prerelease
fi
gh release upload downloader dl/NuvioPlusIPTV.apk -R "$REPO" --clobber

echo "Published ${TAG} to ${REPO}"
ls -la out
