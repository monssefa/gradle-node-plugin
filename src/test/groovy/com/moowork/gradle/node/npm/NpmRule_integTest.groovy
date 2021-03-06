package com.moowork.gradle.node.npm

import com.moowork.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

class NpmRule_integTest
    extends AbstractIntegTest
{
    def 'execute npm_install rule'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }

            node {
                version = "0.10.33"
                npmVersion = "2.1.6"
                download = true
                workDir = file('build/node')
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildTask( 'npm_install' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'Use downloaded npm version'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }
            node {
                npmVersion = "2.1.6"
                download = true
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = build( 'npm_run_--version' )

        then:
        result.output =~ /\n2\.1\.6\n/
        result.task( ':npm_run_--version' ).outcome == TaskOutcome.SUCCESS
    }

    def 'Use local npm installation'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }
            node {
                npmVersion = "2.1.6"
                download = true
            }
        ''' )
        writeEmptyPackageJson()

        when:
        build( 'npm_install_npm@4.0.2' )
        def result = build( 'npm_run_--version' )

        then:
        result.output =~ /\n4\.0\.2\n/
        result.task( ':npm_run_--version' ).outcome == TaskOutcome.SUCCESS
    }

    def 'can execute an npm module using npm_run_'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }

            node {
                version = "5.9.0"
                npmVersion = "3.8.3"
                download = true
            }
        ''' )

        copyResources( 'fixtures/npm-missing/package.json', 'package.json' )

        when:
        def result = buildTask( 'npm_run_echoTest' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'test.txt' )
    }

    def 'succeeds to run npm module using npm_run_ when shrinkwrap contains local npm'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }

            node {
                version = "5.9.0"
                npmVersion = "3.8.3"
                download = true
            }
        ''' )

        copyResources( 'fixtures/npm-present/package.json', 'package.json' )
        copyResources( 'fixtures/npm-present/npm-shrinkwrap.json', 'npm-shrinkwrap.json' )

        when:
        def result = buildTask( 'npm_run_parent' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'child1.txt' )
        fileExists( 'child2.txt' )
        fileExists( 'parent1.txt' )
        fileExists( 'parent2.txt' )
    }

    def 'can execute subtasks using npm'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.moowork.node'
            }
            node {
                download = true
            }
        ''' )
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "scripts": {
                "parent" : "echo 'parent1' > parent1.txt && npm run child1 && npm run child2 && echo 'parent2' > parent2.txt",
                "child1": "echo 'child1' > child1.txt",
                "child2": "echo 'child2' > child2.txt"
            }
        }
        """)

        when:
        def result = buildTask( 'npm_run_parent' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'parent1.txt' )
        fileExists( 'child1.txt' )
        fileExists( 'child2.txt' )
        fileExists( 'parent2.txt' )
    }
}
