locals {
  vault_name = "${var.product}-${var.env}"
  rg_name    = "${var.product}-${var.env}-rg"
  db_name    = "juror"
  db_port    = 5432
}
