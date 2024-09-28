# Game of Life using Scala.js and Webassembly

This project implements a very simple Game of Life frontend,
and packages it as both webassembly ans plain JS using Scala.js 1.17.0's _experimental_ Webassembly backend.

[**Live demo**](https://keynmol.github.io/scalajs-wasm-game-of-life/)

## Developing

1. Run `npm install` once
2. In two separate terminals:
   1. `sbt ~buildFast`
   2. `npm run dev`

Then open http://localhost:5173 and enjoy coding with live reload!

