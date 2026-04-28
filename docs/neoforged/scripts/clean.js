/**
 * Cleanup dynamic documentation sections.
 */
const fs = require('node:fs');
const common = require('./common');

const PRIMER_DOCS_PATH = common.primerDocsPath;
const TOOLCHAIN_PLUGIN_PATH = common.toolchainPluginPath;

// Cleanup primers
if (fs.existsSync(PRIMER_DOCS_PATH)) {
    fs.rmSync(PRIMER_DOCS_PATH, { recursive: true, force: true });
}

// Cleanup toolchain plugins
if (fs.existsSync(TOOLCHAIN_PLUGIN_PATH)) {
    fs.rmSync(TOOLCHAIN_PLUGIN_PATH, { recursive: true, force: true });
}
