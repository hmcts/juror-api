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
