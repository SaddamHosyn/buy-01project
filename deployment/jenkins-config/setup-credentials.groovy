#!/usr/bin/env groovy
// Run this script in: Manage Jenkins → Script Console
// This will programmatically add all required credentials for the pipeline

import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.*

// Get Jenkins instance
def jenkins = Jenkins.instance
def domain = Domain.global()
def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

println "=== Adding Jenkins Credentials ==="
println ""

// 1. Docker Hub Username/Password
try {
    def dockerCreds = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        "docker-hub-credentials",
        "Docker Hub credentials for image push",
        System.getenv('DOCKER_HUB_USERNAME') ?: 'your-docker-username',
        System.getenv('DOCKER_HUB_PASSWORD') ?: 'your-docker-password'
    )
    store.addCredentials(domain, dockerCreds)
    println "✅ Added: docker-hub-credentials (Username/Password)"
} catch (Exception e) {
    println "⚠️ docker-hub-credentials already exists or error: ${e.message}"
}

// 2. Docker Hub Username (Secret Text)
try {
    def dockerUserCreds = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        "docker-hub-username",
        "Docker Hub username for image tagging",
        hudson.util.Secret.fromString(System.getenv('DOCKER_HUB_USERNAME') ?: 'your-docker-username')
    )
    store.addCredentials(domain, dockerUserCreds)
    println "✅ Added: docker-hub-username (Secret Text)"
} catch (Exception e) {
    println "⚠️ docker-hub-username already exists or error: ${e.message}"
}

// 3. Slack Webhook URL (Optional)
if (System.getenv('SLACK_WEBHOOK_URL')) {
    try {
        def slackCreds = new StringCredentialsImpl(
            CredentialsScope.GLOBAL,
            "slack-webhook-url",
            "Slack webhook for build notifications",
            hudson.util.Secret.fromString(System.getenv('SLACK_WEBHOOK_URL'))
        )
        store.addCredentials(domain, slackCreds)
        println "✅ Added: slack-webhook-url (Secret Text)"
    } catch (Exception e) {
        println "⚠️ slack-webhook-url already exists or error: ${e.message}"
    }
} else {
    println "⏭️ Skipping slack-webhook-url (SLACK_WEBHOOK_URL not set)"
}

// 4. Email SMTP Credentials (Optional)
if (System.getenv('EMAIL_USERNAME') && System.getenv('EMAIL_PASSWORD')) {
    try {
        def emailCreds = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            "email-credentials",
            "SMTP credentials for email notifications",
            System.getenv('EMAIL_USERNAME'),
            System.getenv('EMAIL_PASSWORD')
        )
        store.addCredentials(domain, emailCreds)
        println "✅ Added: email-credentials (Username/Password)"
    } catch (Exception e) {
        println "⚠️ email-credentials already exists or error: ${e.message}"
    }
} else {
    println "⏭️ Skipping email-credentials (EMAIL_USERNAME or EMAIL_PASSWORD not set)"
}

println ""
println "=== Credential Setup Complete ==="
println ""
println "Required credentials added:"
println "  - docker-hub-credentials ✅"
println "  - docker-hub-username ✅"
println ""
println "Optional credentials:"
println "  - slack-webhook-url ${System.getenv('SLACK_WEBHOOK_URL') ? '✅' : '⏭️ (skipped)'}"
println "  - email-credentials ${(System.getenv('EMAIL_USERNAME') && System.getenv('EMAIL_PASSWORD')) ? '✅' : '⏭️ (skipped)'}"
println ""
println "To verify: Manage Jenkins → Credentials → System → Global credentials"
println ""
println "=== Environment Variables Usage ==="
println "Set these before running the script:"
println "  export DOCKER_HUB_USERNAME='your-username'"
println "  export DOCKER_HUB_PASSWORD='your-password'"
println "  export SLACK_WEBHOOK_URL='https://hooks.slack.com/services/XXX/YYY/ZZZ'  # Optional"
println "  export EMAIL_USERNAME='smtp-username'  # Optional"
println "  export EMAIL_PASSWORD='smtp-password'  # Optional"
