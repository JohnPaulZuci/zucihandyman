package in.handyman.process.onethread

import java.sql.SQLException
import java.sql.SQLSyntaxErrorException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Objects
import java.util.Properties

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.audit.AuditService
import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess
import javax.mail.Address
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendEMailAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "SENDMAIL";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val mailAsIs: in.handyman.dsl.SendEMail = action.asInstanceOf[in.handyman.dsl.SendEMail]
    val mail: in.handyman.dsl.SendEMail = CommandProxy.createProxy(mailAsIs, classOf[in.handyman.dsl.SendEMail], context)
    var st: Statement = null
    val source = mail.getSource
    val from = mail.getFrom
    val name = mail.getName
    val sql: String = mail.getValue.replaceAll("\"", "")
    val sqlList = sql.split(";")
    val id = context.getValue("process-id")
    val pass = mail.getPass
    val formatter: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    val date: Date = new Date();
    System.out.println(formatter.format(date));
    var subject = mail.getSubject
    subject = subject.concat(" " + formatter.format(date))
    var content = mail.getBody
    var smtpHost = mail.getSmtphost
    var smtpPort = mail.getSmtpport
    //    content = content.concat("<br>Please find the result of the pipeline below:<br>")
    val to = mail.getTo
    val cc = mail.getCc
    val bcc = mail.getBcc
    val signature = mail.getSignature
    val con = ResourceAccess.rdbmsConn(source)
    st = con.createStatement()
    con.setAutoCommit(false)
    var text: String = "<html><head><style>.heading {color: #d63384; text-align:center; } .table-view{text-indent: initial;border-spacing: 2px;border-color: grey;display: table;    border-collapse: collapse;width:max-content; max-width:max-content;margin-bottom: 1rem;background-color: transparent;font-size: 1rem;font-weight: 400;line-height: 1.5;color: #212529;text-align: left;background-color: #fff;margin: 0;font-family:lato;white-space: nowrap} .thead-view{display: table-header-group; vertical-align: middle;border-color: inherit;} .back-view{background-color:#98dcff;}</style></head><body>"
    sqlList.foreach { sql =>
      if (!sql.trim.isEmpty()) {
        logger.info(aMarker, "Transform id#{}, executing script {}", id, sql.trim)
        val statementId = AuditService.insertStatementAudit(actionId, "transform->" + name, context.getValue("process-name"))
        try {
          val rs = st.executeQuery(sql)
          val col: Int = rs.getMetaData.getColumnCount
          val colData = new ArrayList[String]()
          if ((!rs.next())) {
            text = text + "<h5 class='heading'>" + "NO RECORDS PROCESSED TODAY" + "</h5>";
          } else {
            text = text + "<br><table border='1' align='center' class='table-view'> <thead class='thead-view'> <tr align='center'>"
            var i: Int = 1
            while (i <= col) {
              text = text + "<td><b>" + rs.getMetaData.getColumnLabel(i) + "<b></td>"
              colData.add(rs.getMetaData.getColumnLabel(i))
              i = i + 1
            }
            text = text + " </tr></thead><tbody>"
            var colorCount: Int = 1;
            do {
              if (colorCount % 2 == 0) {
                text = text + "<tr class='back-view'>"
              } else {
                text = text + "<tr>"
              }
              colorCount = colorCount + 1
              colData.stream().forEach(entry => {
                if (rs.getObject(entry) != null) {
                  var value = rs.getObject(entry).toString()
                  if (value.contains(".") && Objects.nonNull(value)) {
                    value = value.substring(0, value.indexOf("."));
                  }
                  println(value)
                  text = text + "<td>" + value + "</td>";
                } else {
                  text = text + "<td>" + rs.getObject(entry) + "</td>";
                }

              })
              text = text + "</tr>"
            } while (rs.next());
            text = text + "</tbody></table><br>"
          }

          logger.info(aMarker, "Transform id# {}, executed script {} rows returned {}", statementId.toString(), sql.trim())
          st.clearWarnings();
          rs.close()
        } catch {
          case ex: SQLSyntaxErrorException => {
            logger.error(aMarker, "Stopping execution, General Error executing sql for {} with for campaign {}", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))
            throw ex
          }
          case ex: SQLException => {
            logger.error(aMarker, "Continuing to execute, even though SQL Error executing sql for {} ", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))
          }
          case ex: Throwable => {
            logger.error(aMarker, "Stopping execution, General Error iexecuting sql for {} with for campaign {}", sql, ex)
            detailMap.put(sql.trim + ".exception", ExceptionUtil.completeStackTraceex(ex))
            throw ex
          }
        }
      }
    }
    text = text + "<br><br>Thanks,<br>" + signature + ".<br><br></body></html>"
    content = content.concat(text)
    try {
      var message: Message = null
      message = createMessage
      message.setFrom(new InternetAddress(from))
      setToCcBccRecipients

      message.setSentDate(new Date())
      message.setSubject(subject)
      message.setContent(content, "text/html;charset=utf-8")
      sendMessage

      // throws MessagingException
      def sendMessage {
        Transport.send(message)
      }

      def createMessage: Message = {
        val properties = new Properties()
        properties.put("mail.smtp.host", smtpHost)
        properties.put("mail.smtp.port", smtpPort)
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true")
        properties.put("mail.smtp.starttls.enable", "true") //TLS

        val session = Session.getInstance(properties, new Authenticator() {
          override protected def getPasswordAuthentication = new PasswordAuthentication(from, pass)
        });
        return new MimeMessage(session)
      }

      // throws AddressException, MessagingException
      def setToCcBccRecipients {
        setMessageRecipients(to, Message.RecipientType.TO)
        if (cc != null) {
          setMessageRecipients(cc, Message.RecipientType.CC)
        }
        if (bcc != null) {
          setMessageRecipients(bcc, Message.RecipientType.BCC)
        }
      }

      // throws AddressException, MessagingException
      def setMessageRecipients(recipient: String, recipientType: Message.RecipientType) {
        // had to do the asInstanceOf[...] call here to make scala happy
        val addressArray = buildInternetAddressArray(recipient).asInstanceOf[Array[Address]]
        if ((addressArray != null) && (addressArray.length > 0)) {
          message.setRecipients(recipientType, addressArray)
        }
      }

      // throws AddressException
      def buildInternetAddressArray(address: String): Array[InternetAddress] = {
        // could test for a null or blank String but I'm letting parse just throw an exception
        return InternetAddress.parse(address)
      }

    } catch {
      case ex: SQLException => {
        ex.printStackTrace()
      }
    } finally {
      //      mailStmtfrom.close()
      //      mailDbConnfrom.close()
      detailMap.put("name", name)
      detailMap.put("source", source)
      detailMap.put("ddlSql", sql)
    }

    context

  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val mailAsIs: in.handyman.dsl.SendEMail = action.asInstanceOf[in.handyman.dsl.SendEMail]
    val mail: in.handyman.dsl.SendEMail = CommandProxy.createProxy(mailAsIs, classOf[in.handyman.dsl.SendEMail], context)

    val expression = mail.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      detailMap.putIfAbsent("condition-output", output.toString())
      output
    } finally {
      if (expression != null)
        detailMap.putIfAbsent("condition", "LHS=" + expression.getLhs + ", Operator=" + expression.getOperator + ", RHS=" + expression.getRhs)

    }
  }

  def generateAudit(): java.util.Map[String, String] = {
    this.detailMap
  }

}