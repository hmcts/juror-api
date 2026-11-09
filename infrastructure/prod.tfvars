pgsql_storage_mb    = 262144
pgsql_storage_tier  = "P15"
service_criticality = 4
enable_qpi          = true

pgsql_server_configuration_extra = [
  {
    name  = "random_page_cost"
    value = "1.1"
  },
  {
    name  = "effective_io_concurrency"
    value = "200"
  },
  {
    name  = "maintenance_work_mem"
    value = "1048576"
  },
]

#postgres_pitr = {
#  env          = "prod"
#  name         = "juror-api-pitr"
#  restore_time = "2026-01-01T00:00:00Z"
#  import_object_ids = {
#    "DTS JIT Access Juror DB Admin"                   = "40fa94ec-b4bd-486b-97bc-b65aba129275"
#    "DTS Platform Operations PostgreSQL Admin Access" = "ef17f368-0aa5-43d5-9264-28080f83755e"
#    "jenkins-prod-mi"                                 = "b7167d08-1908-410f-9f0d-ebfb93d248c9"
#    "jenkins-ptl-mi"                                  = "7ef3b6ce-3974-41ab-8512-c3ef4bb8ae01"
#  }
#}
#postgres_use_pitr = false
