/**
 * Utility of common fields and methods used by all scripts.
 */
const path = require('node:path');

const PRIMER_PATH = path.join(process.cwd(), 'primer');
const PRIMER_DOCS_PATH = path.join(PRIMER_PATH, 'docs');
const TOOLCHAIN_PLUGIN_PATH = path.join(process.cwd(), 'toolchain', 'docs', 'plugins');

module.exports = {
    /**
     * The path to the primer section.
     */
    primerPath: PRIMER_PATH,
    /**
     * The path to the docs in the primer section.
     */
    primerDocsPath: PRIMER_DOCS_PATH,
    /**
     * The path to the plugins subsection within the toolchain features section.
     */
    toolchainPluginPath: TOOLCHAIN_PLUGIN_PATH
};
