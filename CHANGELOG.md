# Changelog

## [0.9.0](https://www.github.com/Teletha/bee/compare/v0.8.0...v0.9.0) (2021-07-07)


### Features

* Support annotation processor on maven. ([7053921](https://www.github.com/Teletha/bee/commit/7053921fcfb2f632570d5d13c78be51893e1dab7))


### Bug Fixes

* Integrate CI and Git task. ([4e6b32c](https://www.github.com/Teletha/bee/commit/4e6b32c123188d41c165fd621ee9d018380a486b))
* Remove enable-preview option. ([98e9036](https://www.github.com/Teletha/bee/commit/98e9036435236d33f720e912007b3136c96e4f08))
* Test task can detect the existence of test class. ([454f24c](https://www.github.com/Teletha/bee/commit/454f24c4a2512921bf3e12ec2a6dad0d0d528731))
* Update version. ([33a2f74](https://www.github.com/Teletha/bee/commit/33a2f7409442661a51b1e130905e6cea85974762))

## [0.8.0](https://www.github.com/Teletha/bee/compare/v0.7.0...v0.8.0) (2021-03-28)


### Features

* Add check command on compile task. ([dec2637](https://www.github.com/Teletha/bee/commit/dec2637e8fdda9188570ae747ec3146b99ae7935))
* Add CI/CD task (for github). ([148d146](https://www.github.com/Teletha/bee/commit/148d146bfc82e856733cada9c6dadb5899101a99))
* Enable CI on github. ([06354df](https://www.github.com/Teletha/bee/commit/06354df76b4d92e111bf583971e7933fc8899668))
* IDE task builds pom file synchronously. ([7537381](https://www.github.com/Teletha/bee/commit/753738134e9184f8fdf8dd7069001f2dbc80d653))
* Jar task supports Java16 on version modification. ([d1ce3ea](https://www.github.com/Teletha/bee/commit/d1ce3ea8ca2e4c91411c3bc320aaff0eff64b5ee))
* Provide version control system model. ([adea252](https://www.github.com/Teletha/bee/commit/adea252a1454feaa78f7c3fcc756486c3801684f))
* Terminates all tasks when a test fails. ([e4910a9](https://www.github.com/Teletha/bee/commit/e4910a9dbc1b9bd52e923c2333ac745b63b8d1b8))
* The dependency Grab annotation like Groovy. ([6280a12](https://www.github.com/Teletha/bee/commit/6280a123eba3333dc8ae7d64b461f89d79ea8aa0))
* Use release option on maven compiler plugin. ([7ef2a0d](https://www.github.com/Teletha/bee/commit/7ef2a0d24e9e689cbbea38b0086f48515c7003e5))
* When executing any command, if the project definition has been ([d820b49](https://www.github.com/Teletha/bee/commit/d820b49a9660f50253ee29d0608bc62cdfefe196))


### Bug Fixes

* Build environment restricts the upper bound of java version. ([38cc547](https://www.github.com/Teletha/bee/commit/38cc547b966a961ca6c788f30471112d3b2fa979))
* CI/CD task support JitPack. ([99ff1ce](https://www.github.com/Teletha/bee/commit/99ff1ce1ea02a43987e16a0551185061a5632998))
* Compile at java 11. ([e0b6a8b](https://www.github.com/Teletha/bee/commit/e0b6a8b69a1915b71867a1459248c95b5c253cc1))
* Dependency resolution fails when library is referred from compile ([520b6f7](https://www.github.com/Teletha/bee/commit/520b6f72935099bf3dc04cc48a24ac5f999488f4))
* Disable logging of Junit. ([e148573](https://www.github.com/Teletha/bee/commit/e14857382089965cfdef35c7b2d196376945f024))
* Drop preview support. ([b95f3b0](https://www.github.com/Teletha/bee/commit/b95f3b05565fa991ce017ecc04b8532478cac678))
* Inputs#ref trim its contents. ([86e3415](https://www.github.com/Teletha/bee/commit/86e3415b481c6fbc9e205538c6562ed65a1ae1e9))
* Integrate Inputs and DebugHelper. ([eabe63e](https://www.github.com/Teletha/bee/commit/eabe63ed53236a916d88d3859469166b9410ba5e))
* Jar file is ignored. ([7aef28e](https://www.github.com/Teletha/bee/commit/7aef28e89006ee2315fd598907d056b7eb39f3fa))
* Licesen is class now and remove StandardLicense. ([3d91a52](https://www.github.com/Teletha/bee/commit/3d91a524f89d6b7f1ceca2afba78fd8ebfe939ef))
* Make code compilable by javac. ([5905a48](https://www.github.com/Teletha/bee/commit/5905a48e7ea21b5d9433ceab1b77bd2028e64fe2))
* NotationTest fails on non-windows platform. ([fd20177](https://www.github.com/Teletha/bee/commit/fd2017740f682c0344ca620c421341d38d7d53ea))
* Overridden commands in subclasses were not being recognized. ([d820b49](https://www.github.com/Teletha/bee/commit/d820b49a9660f50253ee29d0608bc62cdfefe196))
* POM uses the ranged dependency version. ([92faf58](https://www.github.com/Teletha/bee/commit/92faf5804ba6c6d8aedd21032299ada717b3dec6))
* Public bee related projects should refer the version.txt. ([2190d36](https://www.github.com/Teletha/bee/commit/2190d36ea4aa0978350002ecafb87fb9c559c9a0))
* Remove PGP. ([11c50ff](https://www.github.com/Teletha/bee/commit/11c50ff9751720066d916f4bd409e4c3e6cbeb4e))
* Remove Task#readResource, use text-block instead. ([06354df](https://www.github.com/Teletha/bee/commit/06354df76b4d92e111bf583971e7933fc8899668))
* Remove unused class. ([148d146](https://www.github.com/Teletha/bee/commit/148d146bfc82e856733cada9c6dadb5899101a99))
* Simplify a failure message. ([dc107c8](https://www.github.com/Teletha/bee/commit/dc107c86940367ae4a7528090714f107364493d4))
* The bee related project' version is corrupted. ([7537381](https://www.github.com/Teletha/bee/commit/753738134e9184f8fdf8dd7069001f2dbc80d653))
* Update pom. ([b99df4e](https://www.github.com/Teletha/bee/commit/b99df4e0bcaace3c9a2e974179010d65c601e015))
* Use java15 on memoized task. ([8402e86](https://www.github.com/Teletha/bee/commit/8402e86fb8fe2f73d442094b6ac170b97921bff0))

## [0.7.0](https://www.github.com/Teletha/bee/compare/0.1.8...v0.7.0) (2021-03-23)


### Features

* Compiler target version of test classes is configurable. ([1735bc3](https://www.github.com/Teletha/bee/commit/1735bc3fdda996934f4d8225a6f981cf21b61d8d))
* Enable release-please-action when you use github. ([e392965](https://www.github.com/Teletha/bee/commit/e392965177691dc2d939f2c5dcb98b055c85698a))
* Enable release-please-action when you use github. ([a75ff91](https://www.github.com/Teletha/bee/commit/a75ff91b0cd98243d6ab3ef2de0e469975819b96))
* Inputs#ref retrieve the file's live contents. ([9f23e93](https://www.github.com/Teletha/bee/commit/9f23e93a1f631d2285e1ab892f5ed6305c13aadd))
* Project#product accepts CharSequence. ([4a61c3a](https://www.github.com/Teletha/bee/commit/4a61c3ac5664c38c2cb385a4dca052fbdc9c27bb))


### Bug Fixes

* Enabel in offline mode. ([979f13b](https://www.github.com/Teletha/bee/commit/979f13b8e861fbbb4e0a25679955de5c03658f3a))
* Exe task is broken. ([f1d7c60](https://www.github.com/Teletha/bee/commit/f1d7c60da29bb3c2764368e86354a0309d7d9119))
* FindMain throws StackOverFlowError. ([fbf65bb](https://www.github.com/Teletha/bee/commit/fbf65bb1d88ee31bea8497487d80c442fd1d56c8))
* Library can recognize classifier. ([525d318](https://www.github.com/Teletha/bee/commit/525d318babfa09e737b55d15c1be790d91f116e8))
* Load same package twice. ([7dd09ce](https://www.github.com/Teletha/bee/commit/7dd09ce8a0c5c7cab06d06fcf312a3c1eb5c6f53))
* Memoized task is defined duplicately at parallel task execution. ([bd1a83a](https://www.github.com/Teletha/bee/commit/bd1a83a90401e6d81982e0264dbb88bc16a246be))
* Process is broken. ([24fd067](https://www.github.com/Teletha/bee/commit/24fd0675beacc1490c4e7ac3a55134568b62395b))
* Release-please-action should execution on main brunch. ([bd1a83a](https://www.github.com/Teletha/bee/commit/bd1a83a90401e6d81982e0264dbb88bc16a246be))
* Task#readResource can't read resource in jar. ([d99ed7c](https://www.github.com/Teletha/bee/commit/d99ed7cc642be00eed54ed61a1602d12f77f700a))
* Task#readResource fails by relative path. ([bd1a83a](https://www.github.com/Teletha/bee/commit/bd1a83a90401e6d81982e0264dbb88bc16a246be))
* Test scope accepts compile scope. ([77978bf](https://www.github.com/Teletha/bee/commit/77978bfd35ada2eff627f722d4f4f1550f13b9d5))
* Use ASM directly instead of byte-buddy. ([2a88f0b](https://www.github.com/Teletha/bee/commit/2a88f0b2dbebf94781aaab1caaaf5d79de5048c4))
