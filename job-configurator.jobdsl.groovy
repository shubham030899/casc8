// Only injected in development mode, assign defaults for production
JOBDSL_INCLUDE = binding.hasVariable("JOBDSL_INCLUDE") ? JOBDSL_INCLUDE : ".*"

pipelineJob("job-configurator") {
    properties {
        disableConcurrentBuilds()
        durabilityHint {
            hint('PERFORMANCE_OPTIMIZED')
        }
        pipelineTriggers {
            triggers {
                gitlab {
                    triggerOnPush(true)
                    triggerOnMergeRequest(false)
                }
            }
        }
    }
    parameters {
        stringParam('CASC_DECLARATION_REPO', CASC_DECLARATION_REPO, 'Git cloneable URL of declaration repository (mapped volume in development mode)')
        stringParam('CASC_DECLARATION_REPO_BRANCH', CASC_DECLARATION_REPO_BRANCH, 'Branch used for Git declaration repository (inactive in development mode)')
        booleanParam('FAIL_ON_CASC_CHANGES', true, 'Fail if casc.yaml or plugins.txt changed since Jenkins start-up')
        stringParam('JOBDSL_INCLUDE', JOBDSL_INCLUDE, "Process only Job DSL files that are matching the regex")
    }

    definition {
        cps {
            if (CASC_DECLARATION_REPO == '/tmp/config') {
                // Invoked locally for _tool_ development. Without this, remote SCM is checked out for seed-job script instead of using local file with possible modifications
                script(new File('/tmp/tool/cci-jd/job-configurator.jenkinsfile').text)
            } else {
                // Prefer inlining over checkout to make sure the Job DSL and jenkinsfile are updated at the same time and jenkinsfile does not get ahead of Job DSL
                script(new URL("https://gitlab.cee.redhat.com/ccit/jenkins-csb/-/raw/${JENKINS_CSB_REPO_BRANCH}/cci-jd/job-configurator.jenkinsfile").text)
            }
            sandbox(true)
        }
    }
}
