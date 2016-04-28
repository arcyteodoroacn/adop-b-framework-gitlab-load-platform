# adop-b-framework-gitlab-load-platform

This serves as a GitLab variant of the Load Platform that comes with the ADOP Generation 5 - https://github.com/Accenture/adop-docker-compose

You can specify the Load Platform of the ADOP Gen5 to pull this repo, or simply create a job that executes Job DSL's.

This custom Load Platform will create a Jenkins job and pulls this custom platform management: https://github.com/arcyteodoroacn/adop-b-framework-gitlab-platform-management

# Editing the ADOP Core Generation 5 to launch a GitLab container

You can add a GItLab container to come with the ADOP Core Generation 5 by adding these lines to the compose files:

This GitLab container is configured to connect to the LDAP as well.

On "adop-docker-compose/docker-compose.yml":

gitlab:
  container_name: gitlab
  restart: always
  image: gitlab/gitlab-ce:latest
  net: ${CUSTOM_NETWORK_NAME}
  ports:
    - "9080:9080"
    - "9443:443"
    - "9022:22"
  expose:
    - "9880"
  environment:
    GITLAB_ROOT_PASSWORD: ${GITLAB_ROOT_PASSWORD} 
    GITLAB_OMNIBUS_CONFIG: |
     external_url 'http://${TARGET_HOST}:9080'
     gitlab_rails['ldap_enabled'] = true
     gitlab_rails['ldap_servers'] = YAML.load <<-'EOS'
       main:
         label: 'LDAP'
         host: ldap
         port: 389
         uid: 'uid'
         method: 'plain'
         bind_dn: 'cn=admin,${LDAP_FULL_DOMAIN}'
         password: '${LDAP_PWD}'
         active_directory: false
         allow_username_or_email_login: false
         block_auto_created_users: false
         base: '${LDAP_FULL_DOMAIN}'
     EOS
	 

On "/etc/logging/syslog/default.yml":

gitlab:
  log_driver: "syslog"
  log_opt:
    syslog-address: "udp://${LOGSTASH_HOST}:25826"
    syslog-tag: "gitlab"
	

On "/etc/volumes/local/default.yml":

gitlab:
  user: root
  volumes:
    - gitlab_config:/etc/gitlab
    - gitlab_logs:/var/log/gitlab
    - gitlab_data:/var/opt/gitlab
	
	
On "/etc/volume/nfs/default.yml":

gitlab:
  user: root
  volume_driver: nfs
  volumes:
    - ${NFS_HOST}/nfs/gitlab/config:/etc/gitlab
    - ${NFS_HOST}/nfs/gitlab/logs:/var/log/gitlab
    - ${NFS_HOST}/nfs/gitlab/data:/var/opt/gitlab