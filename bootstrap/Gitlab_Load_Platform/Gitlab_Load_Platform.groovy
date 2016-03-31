// Jobs
def generateGitlabLoadPlatformJob = freeStyleJob("Gitlab_Load_Platform")
 
// Setup generateBuildPipelineJobs
generateGitlabLoadPlatformJob.with {
		wrappers {
			preBuildCleanup()
		}
		scm {
				git {
						remote {
								url('https://github.com/arcyteodoroacn/adop-b-framework-gitlab-platform-management.git')
								credentials("adop-jenkins-master")
						}	
						branch('*/master')
				}
		}
		steps {
				shell('''#!/bin/bash -ex
						echo -e "Host gitlab\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config
						echo -e "Host innersource.accenture.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config
						
						token="$(curl -X POST "http://gitlab:9080/api/v3/session?login=root&password=gitlab1234" | python -c "import json,sys;obj=json.load(sys.stdin);print obj['private_token'];")"
						
						key=$(cat ~/.ssh/id_rsa.pub)

						curl --header "PRIVATE-TOKEN: $token" -X POST "http://gitlab:9080/api/v3/user/keys" --data-urlencode "title=jenkins@adop-core" --data-urlencode "key=${key}"

						# create platform-management into gitlab
						target_repo_name="platform-management"

						curl --header "PRIVATE-TOKEN: $token" -X POST "http://gitlab:9080/api/v3/projects?name=${target_repo_name}"

						# Create Gerrit repository
						# push the sample codes to the sample Gitlab project
						git remote add adop git@gitlab:root/$target_repo_name.git
						git fetch adop
						git push adop +refs/remotes/origin/*:refs/heads/*
				''')
			dsl{
						external('bootstrap/**/*.groovy')
						ignoreExisting()
			}
		}
}

