The test repository in "repository.zip" is an initialized, but empty repository. Hence, it records a single commit only
(hash "c4b6e2b4757af62139310131fcbfd6d6b38d1225"), which does not introduce any content.

That repository can be used for testing general aspects of the tool and, in particular, for scenario tests, which
provide a specific commit sequence to be applied to a repository. However, each commit sequence must start with a commit
that introduces new files not only content changes as the scenario tests execute "git apply", which may fail, if changed
files in the commit are not available in the repository.