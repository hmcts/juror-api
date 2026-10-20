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

postgres_pitr = {
  env                    = "stg"
  name                   = "juror-api-pitr"
  restore_time           = "2026-10-20T10:45:00Z"
  admin_import_object_id = "3c52c98b-07a3-4a97-92b9-298e86bb1ca9"
}
postgres_use_pitr = true

