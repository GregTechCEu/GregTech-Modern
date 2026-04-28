/**
 * Setup dynamic documentation sections.
 */
const fs = require('node:fs');
const chp = require('node:child_process');
const os = require('node:os');
const path = require('node:path');
const common = require('./common');

/**
 * @typedef {{ directories: Object.<string, string>, files: Object.<string, string>}} Mapper
 */
/**
 * Pulls a repositories and copies over the necessary files and directories to the
 * desired location
 * 
 * @param {string} git The git repository to pull from.
 * @param {string} to The base directory to write the repository data to.
 * @param {Mapper} mapper A map of files and directories to copy `key -> value` relative to the `git` and `to` location.
 * @returns The commit of the branch pulled.
 */
function pullRepository(git, to, mapper) {
    // Get temporary directory
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'repo-'), { recursive: true} );

    // Pull repository to temp
    chp.execSync(`git clone ${git} ${tmpDir}`);
    const commit = chp.execSync(`cd ${tmpDir} && git rev-parse HEAD`, { encoding: 'utf-8' }).trim();

    // Copy directories
    if ('directories' in mapper) {
        for (const [key, value] of Object.entries(mapper['directories'])) {
            const keyLoc = path.join(tmpDir, key);
            const valueLoc = path.join(to, value);
            fs.mkdirSync(valueLoc, { recursive: true });
            fs.cpSync(keyLoc, valueLoc, { recursive: true });
        }
    }

    // Copy files
    if ('files' in mapper) {
        for (const [key, value] of Object.entries(mapper['files'])) {
            const keyLoc = path.join(tmpDir, key);
            const valueLoc = path.join(to, value);
            fs.mkdirSync(valueLoc.substring(0, valueLoc.lastIndexOf(path.sep)), { recursive: true });
            fs.copyFileSync(keyLoc, valueLoc);
        }
    }

    // Delete temp location
    fs.rmSync(tmpDir, { recursive: true, force: true });

    return commit;
}

/**
 * Reads and writes the file data, performing the desired operation on the contents.
 * 
 * @param {(fileData:string) => string} operation The operation to perform on the file contents.
 * @param {string} from The location of the file to read.
 * @param {string} to When present, writes the file to this location. Otherwise, uses `from`.
 */
function readWriteFile(operation, from, to = undefined) {
    // Read file
    var data = fs.readFileSync(from, { encoding: 'utf-8' });
    
    // Use from location if to isn't defined
    if (to === undefined) {
        to = from;
    } else {
        // Delete if to is present
        fs.rmSync(from);
        fs.mkdirSync(to.substring(0, to.lastIndexOf(path.sep)), { recursive: true });
    }

    data = operation(data);

    fs.writeFileSync(to, data);
}

/**
 * Appends a header to an individual file.
 * 
 * @param {string|(fileData:string)=>string} header The header to append, or a function to construct the
 * header from the file text.
 * @param {string} from The location of the file.
 * @param {string} to When present, writes the file to this location. Otherwise, uses `from`.
 */
function appendHeader(header, from, to = undefined) {
    readWriteFile(
        function(data) { return `${typeof header === 'string' ? header : header(data)}\n${data}`; },
        from,
        to
    );
}

/**
 * Modifies a file line-by-line by applying the provided patches.
 * 
 * @param {Array<(line:string, context:Object.<string, string>, extraLineConsumer:(extraLine:string)=>void)=>string>} patches 
 * The patches to apply to each line of the file contents.
 * @param {string} from The location of the file.
 * @param {string} to When present, writes the file to this location. Otherwise, uses `from`.
 */
function modifyFile(patches, from, to = undefined) {
    readWriteFile(
        function (data) {
            // Setup variables
            const output = [];
            const context = {};

            dataLoop:
            for (var line of data.split('\n')) {
                // Check if the line should be skipped
                if ('skip' in context && context['skip']) {
                    context['skip'] = false;
                    continue;
                }

                for (const patch of patches) {
                    line = patch(line, context, function (extraLine) { output.push(extraLine); });
                    // If line is ever undefined, break out of data loop
                    if (line == undefined) {
                        continue dataLoop;
                    }
                }

                // Push current line
                output.push(line);
            }

            return output.join('\n');
        },
        from,
        to
    );
}

const ADMONITION_MAPPER = {
    'IMPORTANT': 'info',
    'WARNING': 'warning',
    'CAUTION': 'warning',
    'NOTE': 'note'
};

/**
 * A patch that modifies an admonition from GitHub to Docusaurus format.
 * 
 * @param {string} line The line being checked.
 * @param {Object.<string, string>} context The current context of the modified file contents.
 * @param {(extraLine:string)=>void} extraLineConsumer A consumer to push an extra line to the output.
 * @returns The line to write.
 */
function admonitionPatch(line, context, extraLineConsumer) {
    const partOfAdmonition = context['partOfAdmonition'];

    // Change admonition to docs format
    if (!partOfAdmonition) {
        const match = line.match(/\[!([A-Z]+)\]/);
        if (match) {
            context['partOfAdmonition'] = true;
            return `:::${match[1] in ADMONITION_MAPPER ? ADMONITION_MAPPER[match[1]] : 'note'}`;
        }
    }

    // Otherwise, if already part of the admonition
    if (partOfAdmonition) {
        if (line.match(/^> /)) {
            return line.substring(2, line.length);
        } else {
            context['partOfAdmonition'] = false;
            extraLineConsumer(':::');
        }
    }

    return line;
}

/**
 * A patch that modifies a link ref to its updated location.
 * 
 * @param {Object.<string, string>} linkMap A map of old links to new links.
 * @param {string} line The line being checked.
 * @returns The line to write.
 */
function linkRemapperPatch(linkMap, line) {
    // Replace links
    const linkMatcher = line.matchAll(/\[[^\]]*\]\(([^\)]+)\)/g);
    for (const match of linkMatcher) {
        const link = match[1];
        const replacementIndex = match.index + match[0].length - link.length - 1;
        if (link in linkMap) {
            line = line.substring(0, replacementIndex) + linkMap[link] + line.substring(replacementIndex + link.length);
        }
    }

    return line;
}

const VERSION_PARSERS = [
    // Pre-Classic
    {
        // Regex to determine whether version matches
        regex: /^rd-[0-9]+$/,
        // Comparison function to match ascending
        compare: (a, b) => parseInt(a.substring(3)) - parseInt(b.substring(3))
    },
    // Full release
    {
        regex: /[0-9]+\.[0-9]+(?:\.[0-9]+)?/,
        compare: (a, b) => {
            // Handle missing third part
            const aVer = a.split('.');
            if (aVer.length == 2) {
                aVer.push('0');
            }
            const bVer = b.split('.');
            if (bVer.length == 2) {
                bVer.push('0');
            }

            // Compare each part
            for (var i = 0; i < 3; i++) {
                if (aVer[i] == bVer[i]) {
                    continue;
                }

                return parseInt(aVer[i]) - parseInt(bVer[i]);
            }

            // Otherwise, assume equal
            return 0;
        }
    }
];

const PRIMER_PATH = common.primerPath;
const TOOLCHAIN_PLUGIN_PATH = common.toolchainPluginPath;

const PRIMERS_GIT = 'https://github.com/neoforged/.github';
const MDG_GIT = 'https://github.com/neoforged/ModDevGradle';
const NG_GIT = 'https://github.com/neoforged/NeoGradle';
const WEBSITES_GIT = 'https://github.com/neoforged/websites';

// Setup primers
const primerDocs = common.primerDocsPath;
if (!fs.existsSync(primerDocs)) {
    const primerCommit = pullRepository(PRIMERS_GIT, PRIMER_PATH, {
        directories: {
            'primers': 'docs'
        }
    });

    // Pull news to get neo changes
    const tmpPath = path.join(process.cwd(), 'tmp');
    pullRepository(WEBSITES_GIT, tmpPath, {
        directories: {
            'content/news': 'websites'
        }
    })

    // Order primers starting from most recent
    const primers = fs.readdirSync(primerDocs, { withFileTypes: true }).map((value) => {
        // Make sure it is a directory
        if (!value.isDirectory()) return null;

        // Map to version parser
        for (const [idx, parser] of VERSION_PARSERS.entries()) {
            if (value.name.match(parser.regex)) return [idx, value.name];
        }
        
        // Otherwise skip
        return null;
    }).filter((possible) => {
        return possible != null;
    }).sort((a, b) => {
        // Compare version parsers
        if (a[0] != b[0]) return -(a[0] - b[0]);

        // Otherwise, compare the versions themselves
        return - (VERSION_PARSERS[a[0]].compare(a[1], b[1]));
    }).map((pair) => pair[1]);
    
    // Loop through primers and apply headers
    const neoNewsVersions = new Set();
    var currentPosition = 2;
    for (const primer of primers) {
        appendHeader(function(fileData) {
            const title = fileData.substring(0, fileData.indexOf('\n'))
            .match(/[^ ]+ -> [^ ]+/)[0];
            return `---\ntitle: ${title}\nsidebar_position: ${currentPosition}\n---`;
        }, path.join(primerDocs, primer, 'index.md'));
        
        // If a neo news article exists for that version, add it
        const versionSegments = primer.split('.');
        const versionExtractor = parseInt(versionSegments[0]) > 1
            ? () => primer
            : () => versionSegments.length == 2 ? `${versionSegments[1]}.0` : `${versionSegments[1]}.${versionSegments[2]}`
        const neoNews =`${versionExtractor()}release`
        if (fs.existsSync(path.join(tmpPath, 'websites', `${neoNews}.md`))) {
            fs.writeFileSync(path.join(primerDocs, primer, 'neo.md'), `---
                title: Neo Changes
                ---

                <iframe src="https://neoforged.net/news/${neoNews}/" width="100%" height="500px">
                    <p>Your browser does not support iframes.</p>
                </iframe>
            `.replace(/^ +/gm, ''));

            neoNewsVersions.add(primer);
        }

        if (fs.existsSync(path.join(primerDocs, primer, 'forge.md'))) {
            appendHeader('---\ntitle: Forge Changes\n---', path.join(primerDocs, primer, 'forge.md'));
        }

        currentPosition++;
    }

    // Rename README to index and append starting sidebar position
    modifyFile([
        function (line, context, extraLineConsumer) {
            const headerPatch = 'headerPatch' in context ? context['headerPatch'] : 0;

            // Add header
            if (headerPatch == 0) {
                // Push to output
                extraLineConsumer('---\nsidebar_position: 1\n---');

                context['headerPatch'] = 1;
            }

            return line;
        },
        function (line, _, _) {
            var output = line;

            // Handle neo versions
            const match = line.match(/[0-9]+(?:\.[0-9]+)*(?:\/[0-9]+(?:\.[0-9]+)*)* \-\> ([0-9]+(?:\.[0-9]+)*(?:\/[0-9]+(?:\.[0-9]+)*)*)/);
            if (match && neoNewsVersions.has(match[1].split('/')[0])) {
                output = `${line}\n    * [Neo Changes](./${match[1].split('/')[0]}/neo.md)`;
            }

            return output;
        },
        function (line, _, _) { return linkRemapperPatch({
            'LICENSE-50AP5UD5': `https://github.com/neoforged/.github/blob/${primerCommit}/primers/LICENSE-50AP5UD5`,
            'LICENSE-CHAMPIONASH5357': `https://github.com/neoforged/.github/blob/${primerCommit}/primers/LICENSE-CHAMPIONASH5357`,
            'LICENSE-WILLIEWILLUS': `https://github.com/neoforged/.github/blob/${primerCommit}/primers/LICENSE-WILLIEWILLUS`
        }, line); }
    ], path.join(primerDocs, 'README.md'), to = path.join(primerDocs, 'index.md'));

    // Delete temp location
    fs.rmSync(tmpPath, { recursive: true, force: true });
}

// Setup toolchain
if (!fs.existsSync(TOOLCHAIN_PLUGIN_PATH)) {
    const pluginDocs = TOOLCHAIN_PLUGIN_PATH;
    fs.mkdirSync(TOOLCHAIN_PLUGIN_PATH, { recursive: true});

    // Category JSON
    fs.writeFileSync(path.join(pluginDocs, '_category_.json'), '{"label": "Plugins", "position": 1}', { encoding: 'utf-8' });

    // ModDevGradle
    const mdgDir = path.join(pluginDocs, 'mdg');
    const mdgCommit = pullRepository(MDG_GIT, mdgDir, {
        directories: {
            'assets': 'assets',
            'docs': 'assets'
        },
        files: {
            'README.md': 'index.md'
        }
    });

    // Modify page
    modifyFile([
        function (line, _, _) {
            // Rename title to ModDevGradle
            return line.match(/Gradle Plugin/) ? '# ModDevGradle' : line;
        },
        function (line, context, _) {
            const backslashPatch = 'backslashPatch' in context ? context['backslashPatch'] : 0;

            // Backslash <> in Version Range Table
            if (backslashPatch == 0) {
                // Find start
                if (line.match(/Range *\| *Meaning/)) {
                    context['backslashPatch'] = backslashPatch + 1;
                }
            } else if (backslashPatch == 1) {
                // Check if end
                if (line.match(/Local *Files/)) {
                    context['backslashPatch'] = backslashPatch + 1;
                } else {
                    return line.replaceAll('<', '\\<').replaceAll('>', '\\>')
                }
            }

            return line;
        },
        admonitionPatch,
        function (line, _, _) { return linkRemapperPatch({
            'BREAKING_CHANGES.md': `https://github.com/neoforged/ModDevGradle/blob/${mdgCommit}/BREAKING_CHANGES.md`,
            './testproject/build.gradle': `https://github.com/neoforged/ModDevGradle/blob/${mdgCommit}/testproject/build.gradle`,
            'docs/idePostSync1.png': 'assets/idePostSync1.png',
            'docs/idePostSync2.png': 'assets/idePostSync2.png',
            'docs/idePostSync3.png': 'assets/idePostSync3.png',
            'src/main/java/net/neoforged/moddevgradle/dsl/RunModel.java': `https://github.com/neoforged/ModDevGradle/blob/${mdgCommit}/src/main/java/net/neoforged/moddevgradle/dsl/RunModel.java`
        }, line); }
    ], path.join(mdgDir, 'index.md'));

    // NeoGradle
    const ngDir = path.join(pluginDocs, 'ng');
    const ngCommit = pullRepository(NG_GIT, ngDir, {
        files: {
            'README.md': 'index.md'
        }
    });

    // Modify page
    modifyFile([
        function (line, context, _) {
            // Remove documentation mention
            if (line.match(/^For a quick start/)) {
                line = line.substring(0, line.indexOf(', or see')) + '.';
                context['skip'] = true;
            }

            return line;
        },
        admonitionPatch,
        function (line, _, _) { return linkRemapperPatch({
            'dsl/common/src/main/groovy/net/neoforged/gradle/dsl/common/runs/run/Run.groovy': `https://github.com/neoforged/NeoGradle/blob/${ngCommit}/dsl/common/src/main/groovy/net/neoforged/gradle/dsl/common/runs/run/Run.groovy`
        }, line); }
    ], path.join(ngDir, 'index.md'));
}
