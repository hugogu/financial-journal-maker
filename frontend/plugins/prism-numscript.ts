import Prism from 'prismjs';

// Numscript language definition for syntax highlighting
Prism.languages.numscript = {
  'comment': /\/\/.*|\/\*[\s\S]*?\*\//,
  'keyword': /\b(?:vars|send|source|destination|remaining|from|to|max|up|to|allowing|over|overdraft|unbounded|kept)\b/,
  'function': /\b(?:send|set_tx_meta)\b/,
  'variable': /\$[a-zA-Z_][a-zA-Z0-9_]*/,
  'account': /@[a-zA-Z0-9:_-]+/,
  'asset': /[A-Z]{3,}\/\d+/,
  'number': /\b\d+(?:\.\d+)?\b/,
  'operator': /[+\-*\/=<>!]+/,
  'punctuation': /[{}()\[\],;]/,
  'string': {
    pattern: /"(?:[^"\\]|\\.)*"/,
    greedy: true
  }
};

export default Prism;
