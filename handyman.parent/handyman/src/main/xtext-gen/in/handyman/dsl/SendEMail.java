/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Send EMail</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.SendEMail#getSource <em>Source</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getSmtphost <em>Smtphost</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getSmtpport <em>Smtpport</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getFrom <em>From</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getPass <em>Pass</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getTo <em>To</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getCc <em>Cc</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getBcc <em>Bcc</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getSubject <em>Subject</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getBody <em>Body</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getSignature <em>Signature</em>}</li>
 *   <li>{@link in.handyman.dsl.SendEMail#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see in.handyman.dsl.DslPackage#getSendEMail()
 * @model
 * @generated
 */
public interface SendEMail extends Action
{
  /**
   * Returns the value of the '<em><b>Source</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Source</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Source</em>' attribute.
   * @see #setSource(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Source()
   * @model
   * @generated
   */
  String getSource();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getSource <em>Source</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Source</em>' attribute.
   * @see #getSource()
   * @generated
   */
  void setSource(String value);

  /**
   * Returns the value of the '<em><b>Smtphost</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Smtphost</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Smtphost</em>' attribute.
   * @see #setSmtphost(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Smtphost()
   * @model
   * @generated
   */
  String getSmtphost();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getSmtphost <em>Smtphost</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Smtphost</em>' attribute.
   * @see #getSmtphost()
   * @generated
   */
  void setSmtphost(String value);

  /**
   * Returns the value of the '<em><b>Smtpport</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Smtpport</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Smtpport</em>' attribute.
   * @see #setSmtpport(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Smtpport()
   * @model
   * @generated
   */
  String getSmtpport();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getSmtpport <em>Smtpport</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Smtpport</em>' attribute.
   * @see #getSmtpport()
   * @generated
   */
  void setSmtpport(String value);

  /**
   * Returns the value of the '<em><b>From</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>From</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>From</em>' attribute.
   * @see #setFrom(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_From()
   * @model
   * @generated
   */
  String getFrom();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getFrom <em>From</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>From</em>' attribute.
   * @see #getFrom()
   * @generated
   */
  void setFrom(String value);

  /**
   * Returns the value of the '<em><b>Pass</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Pass</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Pass</em>' attribute.
   * @see #setPass(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Pass()
   * @model
   * @generated
   */
  String getPass();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getPass <em>Pass</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Pass</em>' attribute.
   * @see #getPass()
   * @generated
   */
  void setPass(String value);

  /**
   * Returns the value of the '<em><b>To</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>To</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>To</em>' attribute.
   * @see #setTo(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_To()
   * @model
   * @generated
   */
  String getTo();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getTo <em>To</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>To</em>' attribute.
   * @see #getTo()
   * @generated
   */
  void setTo(String value);

  /**
   * Returns the value of the '<em><b>Cc</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Cc</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Cc</em>' attribute.
   * @see #setCc(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Cc()
   * @model
   * @generated
   */
  String getCc();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getCc <em>Cc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Cc</em>' attribute.
   * @see #getCc()
   * @generated
   */
  void setCc(String value);

  /**
   * Returns the value of the '<em><b>Bcc</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Bcc</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Bcc</em>' attribute.
   * @see #setBcc(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Bcc()
   * @model
   * @generated
   */
  String getBcc();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getBcc <em>Bcc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Bcc</em>' attribute.
   * @see #getBcc()
   * @generated
   */
  void setBcc(String value);

  /**
   * Returns the value of the '<em><b>Subject</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Subject</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Subject</em>' attribute.
   * @see #setSubject(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Subject()
   * @model
   * @generated
   */
  String getSubject();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getSubject <em>Subject</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Subject</em>' attribute.
   * @see #getSubject()
   * @generated
   */
  void setSubject(String value);

  /**
   * Returns the value of the '<em><b>Body</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Body</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Body</em>' attribute.
   * @see #setBody(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Body()
   * @model
   * @generated
   */
  String getBody();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getBody <em>Body</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Body</em>' attribute.
   * @see #getBody()
   * @generated
   */
  void setBody(String value);

  /**
   * Returns the value of the '<em><b>Signature</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Signature</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Signature</em>' attribute.
   * @see #setSignature(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Signature()
   * @model
   * @generated
   */
  String getSignature();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getSignature <em>Signature</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Signature</em>' attribute.
   * @see #getSignature()
   * @generated
   */
  void setSignature(String value);

  /**
   * Returns the value of the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Value</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Value</em>' attribute.
   * @see #setValue(String)
   * @see in.handyman.dsl.DslPackage#getSendEMail_Value()
   * @model
   * @generated
   */
  String getValue();

  /**
   * Sets the value of the '{@link in.handyman.dsl.SendEMail#getValue <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Value</em>' attribute.
   * @see #getValue()
   * @generated
   */
  void setValue(String value);

} // SendEMail
