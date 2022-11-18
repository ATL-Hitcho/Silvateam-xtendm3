/*
 *Modification area - Business partner
 *Nbr            Date   User id     Description
 *SILDEV-8       220908 FABOST      Controllo coerenza tipi ordine di vendita – mercato cliente
 *Modification area - Customer
 *Nbr            Date   User id     Description
 *99999999999999 999999 XXXXXXXXXX  x
 */

public class OIS100_PACHK_PRE extends ExtendM3Trigger {
  private final ProgramAPI program
  private final InteractiveAPI interactive
  private final DatabaseAPI database
  private String okcfc4
  private String opsple
  private String ogotyg

  public OIS100_PACHK_PRE(ProgramAPI program, InteractiveAPI interactive, DatabaseAPI database) {
    this.program = program
    this.interactive = interactive
    this.database = database
  }

  public void main() {
    if (interactive.display.fields.WWOPT2 == "1" && interactive.display.fields.OAORTP != "" ||
      interactive.display.fields.WWOPT2 == "" && interactive.display.fields.OAORNO == "" && interactive.display.fields.OAORTP != "") {

      // Check customer number  
      String DSP_OACUNO = interactive.display.fields.OACUNO
      if (DSP_OACUNO.isEmpty()) {
        //   MSGID=WCU0202 Customer must be entered
        interactive.showError("OACUNO", "WCU0202")
      }
      if (!customerExists(program, database, DSP_OACUNO)) {
        //   MSGID=WCU0203 Customer &1 does not exist
        interactive.showError("OACUNO", "WCU0203", DSP_OACUNO)
      }

      this.ogotyg = this.okcfc4
      // Check address number
      String DSP_OAADID = interactive.display.fields.OAADID
      if (DSP_OAADID != "") {
        if (!addressTypeExists(program, database, DSP_OACUNO, 1, DSP_OAADID)) {
          //   MSGID=WAD1003 Address number &1 does not exist
          interactive.showError("OAADID", "WAD1003", DSP_OAADID)
        }
        this.ogotyg = this.opsple
      }

      // Check order type
      String DSP_OAORTP = interactive.display.fields.OAORTP
      if (!orderTypeExists(program, database, DSP_OAORTP)) {
        //   MSGID=WOT9003 Order type &1 does not exist
        interactive.showError("OAORTP", "WOT9003", DSP_OAORTP)
      }
      if (!orderTypeGroupExists(program, database, this.ogotyg, DSP_OAORTP)) {
        //   MSGID=WOT9001 Order type &1 is invalid
        interactive.showError("OAORTP", "WOT9001", DSP_OAORTP)
      }
    }
  }

  public boolean customerExists(ProgramAPI program, DatabaseAPI database, String cuno) {
    DBAction readOCUSMA00 = database.table("OCUSMA").index("00").selection("OKCONO", "OKCUNO", "OKCFC4").build()
    DBContainer containerOCUSMA00 = readOCUSMA00.getContainer()
    containerOCUSMA00.set("OKCONO", program.getLDAZD().CONO)
    containerOCUSMA00.set("OKCUNO", cuno)
    if (readOCUSMA00.read(containerOCUSMA00)) {
      this.okcfc4 = containerOCUSMA00.get("OKCFC4")
      return true
    }
    return false
  }

  public boolean orderTypeExists(ProgramAPI program, DatabaseAPI database, String ortp) {
    DBAction readOOTYPE00 = database.table("OOTYPE").index("00").selection("OOCONO", "OOORTP").build()
    DBContainer containerOOTYPE00 = readOOTYPE00.getContainer()
    containerOOTYPE00.set("OOCONO", program.getLDAZD().CONO)
    containerOOTYPE00.set("OOORTP", ortp)
    if (readOOTYPE00.read(containerOOTYPE00)) {
      return true
    }
    return false
  }

  public boolean orderTypeGroupExists(ProgramAPI program, DatabaseAPI database, String otyg, String ortp) {
    DBAction readOOTYPG00 = database.table("OOTYPG").index("00").selection("OGCONO", "OGOTYG", "OGORTP").build()
    DBContainer containerOOTYPG00 = readOOTYPG00.getContainer()
    containerOOTYPG00.set("OGCONO", program.getLDAZD().CONO)
    containerOOTYPG00.set("OGOTYG", otyg)
    containerOOTYPG00.set("OGORTP", ortp)
    if (readOOTYPG00.read(containerOOTYPG00)) {
      return true
    }
    return false
  }

  public boolean addressTypeExists(ProgramAPI program, DatabaseAPI database, String cuno, int adrt, String adid) {
    DBAction readOCUSAD00 = database.table("OCUSAD").index("00").selection("OPCONO", "OPCUNO", "OPADRT", "OPADID", "OPSPLE").build()
    DBContainer containerOCUSAD00 = readOCUSAD00.getContainer()
    containerOCUSAD00.set("OPCONO", program.getLDAZD().CONO)
    containerOCUSAD00.set("OPCUNO", cuno)
    containerOCUSAD00.set("OPADRT", adrt)
    containerOCUSAD00.set("OPADID", adid)
    if (readOCUSAD00.read(containerOCUSAD00)) {
      this.opsple = containerOCUSAD00.get("OPSPLE")
      return true
    }
    return false
  }
}