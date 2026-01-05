// Jenkins Job DSL Script for Buy-01 CI/CD Pipeline
// This script automatically creates the pipeline job

pipelineJob('buy-01-cicd-pipeline') {
    description('CI/CD Pipeline for Buy-01 E-Commerce Platform - Automated build, test, and deployment')
    
    // Keep build history
    logRotator {
        daysToKeep(30)
        numToKeep(50)
        artifactDaysToKeep(7)
        artifactNumToKeep(10)
    }
    
    // Parameters
    parameters {
        choiceParam('DEPLOY_ENV', ['local', 'staging', 'production'], 'Select deployment environment')
        booleanParam('SKIP_TESTS', false, 'Skip tests (use with caution)')
        booleanParam('FORCE_DEPLOY', false, 'Force deployment even if no changes')
    }
    
    // GitHub project
    properties {
        githubProjectUrl('https://github.com/SaddamHosyn/buy-01project')
    }
    
    // Pipeline definition from SCM
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/SaddamHosyn/buy-01project.git')
                    }
                    branches('*/main')
                    extensions {
                        wipeWorkspace()
                    }
                }
            }
            scriptPath('deployment/Jenkinsfile')
        }
    }
    
    // Triggers
    triggers {
        scm('H/5 * * * *')  // Poll SCM every 5 minutes
    }
    
    // Disable concurrent builds
    configure { project ->
        project / 'properties' / 'org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty' {}
    }
}

// Create a view for the pipeline
listView('Buy-01 Pipeline') {
    description('CI/CD Pipeline for Buy-01 E-Commerce Platform')
    jobs {
        name('buy-01-cicd-pipeline')
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

println "✅ Pipeline job 'buy-01-cicd-pipeline' created successfully!"
println "✅ View 'Buy-01 Pipeline' created successfully!"
println ""
println "Next steps:"
println "1. Configure credentials (see JENKINS-SECURITY.md)"
println "2. Run the pipeline: Dashboard → buy-01-cicd-pipeline → Build with Parameters"
