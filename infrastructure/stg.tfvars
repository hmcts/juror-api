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
#  env          = "stg"
#  name         = "juror-api-pitr"
#  restore_time = "2026-10-20T10:45:00Z"
#  import_object_ids = {
#    "DTS JIT Access Juror DB Admin"                   = "40fa94ec-b4bd-486b-97bc-b65aba129275"
#    "DTS Platform Operations PostgreSQL Admin Access" = "3c52c98b-07a3-4a97-92b9-298e86bb1ca9"
#    "jenkins-stg-mi"                                  = "df14f052-7865-49c6-b6f9-13e123dd8b66"
#    "jenkins-ptl-mi"                                  = "7ef3b6ce-3974-41ab-8512-c3ef4bb8ae01"
#  }
#}
#postgres_use_pitr = true

