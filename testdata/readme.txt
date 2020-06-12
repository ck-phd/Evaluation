############################
# repository_corrupted.zip #
############################
Actually a copy of "repository.zip", which was manually manipulated by deleting some of the raw data (using a text
editor) to corrupt it. This manipulation leads to errors during extraction, which is exploited in some tests to check
the correct error handling of the tool.

##################
# repository.zip #
##################
The test repository in "repository.zip" is an initialized repository, which contains a single, empty "TextFile.txt".
Hence, it records a single commit only (hash "c4b6e2b4757af62139310131fcbfd6d6b38d1225").

That repository can be used for testing general aspects of the tool and, in particular, for scenario tests, which
provide a specific commit sequence to be applied to a repository. However, each commit sequence must start with a commit
that introduces new files or changes to "TextFile.txt" only as the scenario tests execute "git apply", which may fail,
if changed files in the commit are not available in the repository.

######################
# TestRepository.zip #
######################
The repository used for creating the test data. It records a set of commits on different branches as an example for the
typical committing to and branching of Git repositories. It is not explicitly used in the tool development or its tests,
but is included here for documentation.      