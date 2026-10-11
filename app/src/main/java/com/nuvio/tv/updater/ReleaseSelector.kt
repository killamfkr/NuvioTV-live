package com.nuvio.tv.updater

import com.nuvio.tv.data.remote.dto.GitHubReleaseDto

internal object ReleaseSelector {
    private val prereleaseNamePattern = Regex(
        "(?:^|[\\s._-])(alpha|beta|rc|preview)(?:[\\s._-]|$)",
        RegexOption.IGNORE_CASE
    )

    private val utilityTagPattern = Regex("^(downloader|latest)$", RegexOption.IGNORE_CASE)

    fun eligibleReleases(
        releases: List<GitHubReleaseDto>,
        channel: UpdateChannel
    ): List<GitHubReleaseDto> = releases
        .asSequence()
        .filterNot(GitHubReleaseDto::draft)
        .filterNot(::isUtilityRelease)
        .mapNotNull { release ->
            val version = releaseVersion(release) ?: return@mapNotNull null
            ReleaseCandidate(
                release = release,
                version = version,
                prerelease = isPrerelease(release, version)
            )
        }
        .filter { candidate -> channel == UpdateChannel.BETA || !candidate.prerelease }
        .sortedByDescending(ReleaseCandidate::version)
        .map(ReleaseCandidate::release)
        .toList()

    private fun isUtilityRelease(release: GitHubReleaseDto): Boolean {
        val tag = VersionUtils.normalize(release.tagName)
        return tag.isNotEmpty() && utilityTagPattern.matches(tag)
    }

    private fun releaseVersion(release: GitHubReleaseDto): SemanticVersion? =
        VersionUtils.parse(release.tagName) ?: VersionUtils.parse(release.name)

    private fun isPrerelease(
        release: GitHubReleaseDto,
        version: SemanticVersion
    ): Boolean = release.prerelease ||
        version.prerelease.isNotEmpty() ||
        prereleaseNamePattern.containsMatchIn(release.name.orEmpty())

    private data class ReleaseCandidate(
        val release: GitHubReleaseDto,
        val version: SemanticVersion,
        val prerelease: Boolean
    )
}
