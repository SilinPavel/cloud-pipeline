import java.util.Objects;
                    gitReaderDiff.getEntries().stream()
                            .filter(entry -> Objects.nonNull(entry.getDiff()))
                            .flatMap(diff -> {
                                final String[] diffsByFile = diff.getDiff().split(DIFF_GIT_PREFIX);
                                return Arrays.stream(diffsByFile)
                                        .filter(org.apache.commons.lang.StringUtils::isNotBlank)
                                        .map(fileDiff -> {
                                            final GitParsedDiffEntry.GitParsedDiffEntryBuilder fileDiffBuilder =
                                                    GitParsedDiffEntry.builder().commit(
                                                            diff.getCommit().toBuilder()
                                                                    .authorDate(
                                                                        new Date(diff.getCommit().getAuthorDate()
                                                                                .toInstant()
                                                                        .plus(reportFilters.getUserTimeOffsetInMin(),
                                                                                ChronoUnit.MINUTES).toEpochMilli()))
                                                                    .committerDate(
                                                                        new Date(diff.getCommit().getCommitterDate()
                                                                                .toInstant()
                                                                        .plus(reportFilters.getUserTimeOffsetInMin(),
                                                                                ChronoUnit.MINUTES).toEpochMilli())
                                                            ).build());
                                            try {
                                                final Diff parsed = diffParser.parse(
                                                        (DIFF_GIT_PREFIX + fileDiff).getBytes(StandardCharsets.UTF_8)
                                                ).stream().findFirst().orElseThrow(IllegalArgumentException::new);
                                                return fileDiffBuilder.diff(DiffUtils.normalizeDiff(parsed)).build();
                                            } catch (IllegalArgumentException | IllegalStateException e) {
                                                // If we fail to parse diff with diffParser lets
                                                // try to parse it as binary diffs
                                                return fileDiffBuilder.diff(
                                                        DiffUtils.parseBinaryDiff(DIFF_GIT_PREFIX + fileDiff))
                                                        .build();
                                            }
                                        });
                            }).collect(Collectors.toList())