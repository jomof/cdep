## CDep coordinate resolution
A CDep coordinate uniquely identifies a package. It also contains the information needed to reconstruct a URL to the package's manifest. Here's an example:

```
com.github.jomof:sqlite:3.16.2-rev51
```

There are multiple coordinate forms that are used to solve different problems.

### Simple form
The simplest coordinate form looks like this:
```
com.github.jomof:sqlite:3.16.2-rev51
```
During resolution, CDep decomposes this into pieces.
```
domain = github.com
user = jomof
artifact = sqlite
version = 3.16.2-rev51
```
CDep then recomposes these pieces into a URL:
```
https://github.com/jomof/sqlite/releases/download/3.16.2-rev53/cdep-manifest.yml
```
The purpose of the simple form of coordinate is just to be able to host the source and releases for a single CDep package.

### Compound form
Sometimes you want a family of packages under a single umbrella repo. Coordinates of this form look like this:
```
com.github.jomof:firebase/admob:2.1.3-rev22
com.github.jomof:firebase/analytics:2.1.3-rev22
```
These are decomposed in a manner similar to the simple form. The single difference is that the artifact family name is extracted:
```
domain = github.com
user = jomof
family = firebase
artifact = admob
version = 2.1.3-rev22
```
