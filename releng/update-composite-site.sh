#!/usr/bin/env bash
#
# Regenerates the p2 composite metadata at the root of the update site from the
# directories present under <site>/releases, optionally dropping older releases
# first.
#
# Usage: releng/update-composite-site.sh [--keep <n>] [--only <version>] <site-directory>

set -euo pipefail

usage="Usage: $0 [--keep <n>] [--only <version>] <site-directory>"
keep=
only=

while [ $# -gt 0 ]; do
	case $1 in
	--keep)
		keep=${2:?$usage}
		case $keep in
		'' | *[!0-9]* | 0) echo "--keep needs a positive number, got '$keep'" >&2; exit 1 ;;
		esac
		shift 2
		;;
	--only)
		only=${2:?$usage}
		shift 2
		;;
	-*)
		echo "$usage" >&2
		exit 1
		;;
	*)
		[ -n "${site:-}" ] && { echo "$usage" >&2; exit 1; }
		site=$1
		shift
		;;
	esac
done

site=${site:?$usage}
releases_dir="$site/releases"

if [ ! -d "$releases_dir" ]; then
	echo "No releases directory in $site" >&2
	exit 1
fi

# oldest first, so that the newest release is the last child p2 sees
mapfile -t versions < <(find "$releases_dir" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort -V)

if [ ${#versions[@]} -eq 0 ]; then
	echo "No releases below $releases_dir" >&2
	exit 1
fi

if [ -n "$only" ]; then
	found=
	for version in "${versions[@]}"; do
		if [ "$version" = "$only" ]; then
			found=yes
		fi
	done
	if [ -z "$found" ]; then
		echo "No release '$only' below $releases_dir, refusing to delete the others" >&2
		exit 1
	fi
	for version in "${versions[@]}"; do
		if [ "$version" != "$only" ]; then
			echo "Dropping release $version"
			rm -rf "${releases_dir:?}/$version"
		fi
	done
	versions=("$only")
fi

if [ -n "$keep" ] && [ ${#versions[@]} -gt "$keep" ]; then
	drop=$(( ${#versions[@]} - keep ))
	for version in "${versions[@]:0:$drop}"; do
		echo "Dropping release $version"
		rm -rf "${releases_dir:?}/$version"
	done
	versions=("${versions[@]:$drop}")
fi

# p2 expects milliseconds; %3N is not honoured by every coreutils implementation
timestamp=$(( $(date +%s) * 1000 ))

write_composite() {
	local file=$1 processing_instruction=$2 type=$3 name=$4
	{
		printf "<?xml version='1.0' encoding='UTF-8'?>\n"
		printf "<?%s version='1.0.0'?>\n" "$processing_instruction"
		printf "<repository name='%s' type='%s' version='1.0.0'>\n" "$name" "$type"
		printf "  <properties size='2'>\n"
		printf "    <property name='p2.timestamp' value='%s'/>\n" "$timestamp"
		printf "    <property name='p2.atomic.composite.loading' value='true'/>\n"
		printf "  </properties>\n"
		printf "  <children size='%s'>\n" "${#versions[@]}"
		for version in "${versions[@]}"; do
			printf "    <child location='releases/%s'/>\n" "$version"
		done
		printf "  </children>\n"
		printf "</repository>\n"
	} > "$file"
}

write_composite "$site/compositeContent.xml" compositeMetadataRepository \
	org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository \
	'Eclipse 4 Perspective Switcher'
write_composite "$site/compositeArtifacts.xml" compositeArtifactRepository \
	org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository \
	'Eclipse 4 Perspective Switcher'

cat > "$site/p2.index" <<'EOF'
version=1
metadata.repository.factory.order=compositeContent.xml,!
artifact.repository.factory.order=compositeArtifacts.xml,!
EOF

# keeps GitHub Pages from running the content through Jekyll
touch "$site/.nojekyll"

base_url=https://vogellacompany.github.io/e4-perspective-switcher
cp "$(dirname "$0")/../docs/images/sample-application.png" "$site/sample-application.png"
badges=
for version in "${versions[@]}"; do
	badges+="<span class=\"version\">$version</span> "
done
{
	cat <<EOF
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Perspective Switcher Update Site</title>
<style>
  :root {
    --bg: #f6f5fb;
    --panel: #ffffff;
    --border: #dcd8ee;
    --text: #23203a;
    --muted: #625d80;
    --accent: #2c2255;
    --orange: #f7941e;
  }
  @media (prefers-color-scheme: dark) {
    :root {
      --bg: #12101f;
      --panel: #1c192f;
      --border: #332d55;
      --text: #e4e1f5;
      --muted: #9a95bd;
      --accent: #b9adff;
    }
  }
  * { box-sizing: border-box; }
  body {
    margin: 0;
    background: var(--bg);
    color: var(--text);
    font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
    line-height: 1.6;
  }
  main { max-width: 48rem; margin: 0 auto; padding: 3.5rem 1rem 3rem; }
  h1 { margin: 0; font-size: clamp(2rem, 6vw, 2.8rem); line-height: 1.15; color: var(--accent); }
  .tagline { margin: .5rem 0 0; color: var(--muted); font-size: 1.1rem; }
  h2 {
    font-size: .8rem;
    letter-spacing: .18em;
    text-transform: uppercase;
    color: var(--muted);
    margin: 2.5rem 0 .8rem;
  }
  .url {
    display: flex;
    align-items: center;
    gap: .8rem;
    padding: .8rem 1rem;
    background: var(--panel);
    border: 1px solid var(--border);
    border-left: 4px solid var(--orange);
    border-radius: .5rem;
  }
  .url code { flex: 1; font-size: 1rem; overflow-wrap: anywhere; }
  button {
    font: inherit;
    font-size: .85rem;
    padding: .35rem .9rem;
    color: #fff;
    background: var(--orange);
    border: 0;
    border-radius: .4rem;
    cursor: pointer;
  }
  button:hover { filter: brightness(1.08); }
  figure { margin: 2rem 0 0; }
  figure img {
    width: 100%;
    height: auto;
    border: 1px solid var(--border);
    border-radius: .5rem;
    box-shadow: 0 .6rem 2rem #2c225522;
  }
  figcaption { color: var(--muted); font-size: .9rem; margin-top: .5rem; }
  ol { padding-left: 1.2rem; }
  li { margin: .3rem 0; }
  em { font-style: normal; font-weight: 600; }
  pre {
    background: var(--panel);
    border: 1px solid var(--border);
    border-radius: .5rem;
    padding: .8rem 1rem;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
    font-size: .85rem;
  }
  .version {
    display: inline-block;
    padding: .15rem .7rem;
    border: 1px solid var(--border);
    border-radius: 999px;
    background: var(--panel);
    font-family: ui-monospace, monospace;
  }
  .note { color: var(--muted); font-size: .9rem; }
  a { color: var(--accent); }
  footer { margin-top: 3rem; padding-top: 1.2rem; border-top: 1px solid var(--border); color: var(--muted); font-size: .9rem; }
</style>
</head>
<body>
<main>
<h1>Eclipse 4 Perspective Switcher</h1>
<p class="tagline">A perspective switcher tool control for Eclipse 4 RCP applications.</p>

<figure>
  <img src="sample-application.png" alt="The perspective switcher in the top right corner of the sample application" width="900" height="600">
  <figcaption>The switcher in the trim bar of the sample application, with perspective names shown.</figcaption>
</figure>

<h2>Update site</h2>
<div class="url">
  <code id="site-url">$base_url/</code>
  <button type="button" onclick="const b = this; Promise.resolve().then(() => navigator.clipboard.writeText(document.getElementById('site-url').textContent)).then(() => { b.textContent = 'Copied'; }, () => { b.textContent = 'Copy failed, select the URL'; })">Copy</button>
</div>

<h2>Use it in your application</h2>
<ol>
  <li>Add the update site to your target platform and include the feature <em>org.eclipse.e4.ui.workbench.perspectiveswitcher.feature</em> in your product.</li>
  <li>Add a tool control to a trim bar of your application model that points to the switcher:</li>
</ol>
<pre>bundleclass://org.eclipse.e4.ui.workbench.perspectiveswitcher/org.eclipse.e4.ui.workbench.perspectiveswitcher.tools.PerspectiveSwitcherSwtTrim</pre>

<h2>Current build</h2>
<p>$badges</p>
<p class="note">This site carries the newest build only. Older builds are attached as repository archives to the GitHub releases.</p>

<footer>Sources and documentation: <a href="https://github.com/vogellacompany/e4-perspective-switcher">github.com/vogellacompany/e4-perspective-switcher</a></footer>
</main>
</body>
</html>
EOF
} > "$site/index.html"

echo "Composite site updated with ${#versions[@]} release(s): ${versions[*]}"
