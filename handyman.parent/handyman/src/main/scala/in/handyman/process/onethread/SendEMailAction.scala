package in.handyman.process.onethread

import java.sql.SQLException
import java.util.Date
import java.util.Properties

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.util.ParameterisationEngine
import javax.mail.Address
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import in.handyman.util.ResourceAccess
import java.sql.Statement
import java.util.ArrayList
import java.text.SimpleDateFormat

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
    val id = context.getValue("process-id")
    val pass = mail.getPass
    val formatter: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    val date: Date = new Date();
    System.out.println(formatter.format(date));
    var subject = mail.getSubject
    subject = subject.concat(" " + formatter.format(date))
    var content = "Hi Team! ,<br><br>"
    content = content.concat("<br>Please find the result of the pipeline below:<br>")
    //    content = content+"\r\nPlease find the result of the pipeline below:\r\n"
    val to = mail.getTo
    val cc = mail.getCc
    val bcc = mail.getBcc
    val con = ResourceAccess.rdbmsConn(source)
    st = con.createStatement()
    con.setAutoCommit(false)
    var text =
      "<br><table width='100%' border='1' align='center'> <tr align='center'>"
    val rs = st.executeQuery(sql)
    val col: Int = rs.getMetaData.getColumnCount
    val colData = new ArrayList[String]()
    var i: Int = 1
    while (i <= col) {
      text = text + "<td><b>" + rs.getMetaData.getColumnLabel(i) + "<b></td>"
      colData.add(rs.getMetaData.getColumnLabel(i))
      i = i + 1
    }
    text = text + " </tr>"

    while (rs.next) {
      text = text + "<tr align='center'>"
      colData.stream().forEach(entry => {
        text = text + "<td>" + rs.getObject(entry) + "</td>";
      })
      text = text + "</tr>"
    }
    if ((!rs.isBeforeFirst() && rs.getRow() == 0)) {
      text = text + "<td>" + "NO RECORDS PROCESSED" + "</td>";
      text = text + "</tr>"
    }
    text = text + "</table><br><br><br>Thanks,<br>INTEGRATION TEAM.<br><br>"
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
        properties.put("mail.smtp.host", "smtp.gmail.com")
        properties.put("mail.smtp.port", "587")
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