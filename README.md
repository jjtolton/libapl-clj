[![Clojars Project](https://img.shields.io/clojars/v/jjtolton/libapl-clj.svg)](https://clojars.org/jjtolton/libapl-clj)

# libapl-clj

Following in the footsteps of [libpython-clj](https://github.com/clj-python/libpython-clj)
and [libjulia-clj](https://github.com/cnuernber/libjulia-clj), libapl-clj is provides native
interop from Clojure into [GNU APL](https://www.gnu.org/software/apl/).

## Status

MVP: APL shared library is able to be loaded on Linux. Arbitrary APL strings can be run,
and the values can be extracted into strings. No marshalling to Clojure datastructures
currently supported, but that's next on the roadmap. API and implementation subject to
change.

## Overview

APL is a fantastic tensor processing language with a ton of power. It's work has been
hugely inspirational to libraries like Python's [numpy](https://numpy.org/) and
[dtype-next](https://cnuernber.github.io/dtype-next/).

The aim of this library is to provide native interop for APL from Clojure.

## Usage

Tested on Linux Mint 19. Should also work on MacOS and probably Windows if you
know how to set environment variables (see below).

1. Follow the directions to [download GNU APL](https://www.gnu.org/software/apl/).
2. When installing APL, use the following options:
```bash
make develop_lib
sudo make install
```
3. The default shared library installation path is `/usr/local/lib/apl`. The filename
is `libapl.so`. If `libapl.so` is installed somewhere else, please set your `APL_LIBRARY_PATH`
environment variable to the correct path before running `initialize!`.

## Example

```clojure
(require '[libapl-clj.apl :as apl])
(apl/initialize!)
;;=> :ok
(apl/run-simple-string! "res ← 4 4 ⍴ 3")
;;=> true
(def res (apl/value-pointer "res"))
res
;;=> #object[com.sun.jna.Pointer 0x5ca7c618 "native@0x7ff08049f730"]
(pointer->string res)
;;=> "3 3 3 3\n3 3 3 3\n3 3 3 3\n3 3 3 3\n"
(pointer->rank res)
;;=> 2
(pointer->count res)
;;=> 16
```

## Roadmap

* [ ] Push to Clojars 
* [ ] Marshall pointers to native Clojure datatypes 
* [ ] Zero-copy pathway between APL and Clojure for monster performance 
* [ ] Ergonomic Clojure API
