# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2024-10-21
### Changed
- Recommended minimum Gradle version is now 8.8.

### Fixed
- Address configuration cache problems.

## [0.4.0] - 2024-05-07
### Added
- Allow to filter out translated files when copying them.

### Changed
- Recommended minimum Gradle version is now 8.5.
- Maintenance changes.

## [0.3.1] - 2023-01-25
### Fixed
- Correctly include dependencies when publishing to the Gradle Plugin Portal.

## [0.3.0] - 2023-01-25
### Changed
- Recommended minimum Gradle version is now 7.5.1.
- The minimum Java version is 11.
- Update Crowdin API client to support newer API responses.

## [0.2.1] - 2021-08-12
### Fixed
- Copy only translations that are contained in the export paths, if several export
  paths had a common prefix some translations could be copied to incorrect directory.

## [0.2.0] - 2021-07-30
### Added
- A task to list the translation progress, `crowdinListTranslationProgress`.

### Fixed
- Ignore empty source files.

## [0.1.0] - 2021-07-26
First alpha release.

### Added
A plugin with ID `org.zaproxy.crowdin`.

The plugin provides the following features:

#### Extension
 - `crowdin` to specify the authentication token and the configuration file.

#### Tasks
Added by the plugin:
 - `crowdinBuildProjectTranslation` - Builds the project translation package.
 - `crowdinCopyProjectTranslations` - Copies the project translations to respective directories.
 - `crowdinDownloadProjectTranslation` - Downloads the latest project translation package.
 - `crowdinListAllFiles` - Lists all the files in Crowdin.
 - `crowdinListFiles` - Lists the files in Crowdin (that match the configuration).
 - `crowdinListSourceFiles` - Lists the source files.
 - `crowdinUploadSourceFiles` - Uploads the source files to Crowdin.


[0.5.0]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.3.1...v0.4.0
[0.3.1]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.2.1...v0.3.0
[0.2.1]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/zaproxy/gradle-plugin-crowdin/compare/f935566adf4ba84f9a15def93643ef2d482ee2fc...v0.1.0
