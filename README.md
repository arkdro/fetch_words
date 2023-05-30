# fetch_words

- take an input file
- extract words of the original language
- for every word
  - fetch an audio file
  - create a sub-directory based on the number of levels.
  - save the audio file to the output sub-directory

## Usage

```
lein run -- -w data.txt -o /tmp/out -l 3 -i 200 -a 1000
```

### Parameters

`-w` - file with input data.
       It is a `sqlite` dump file with `INSERT INTO notes VALUES` lines.

`-o` - base output directory

`-l` - upper limit of how many sub-directory levels to create.
       Creating sub-directories uses 2 characters per level.
       E.g. the word `catalog` and `--levels=3` results in `ca/ta/log`

`-i` - minimum delay in milliseconds

`-a` - maximum delay in milliseconds

If delay parameters are used, then the total delay is randomized in the interval
`[min ... max)`

## License

Copyright © 2023 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
