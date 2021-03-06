/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.LoadCsv;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Load Csv</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getPid <em>Pid</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getSource <em>Source</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getTo <em>To</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getDelim <em>Delim</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getLimit <em>Limit</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.LoadCsvImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class LoadCsvImpl extends ActionImpl implements LoadCsv
{
  /**
   * The default value of the '{@link #getPid() <em>Pid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPid()
   * @generated
   * @ordered
   */
  protected static final String PID_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getPid() <em>Pid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPid()
   * @generated
   * @ordered
   */
  protected String pid = PID_EDEFAULT;

  /**
   * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSource()
   * @generated
   * @ordered
   */
  protected static final String SOURCE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSource()
   * @generated
   * @ordered
   */
  protected String source = SOURCE_EDEFAULT;

  /**
   * The default value of the '{@link #getTo() <em>To</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTo()
   * @generated
   * @ordered
   */
  protected static final String TO_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getTo() <em>To</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTo()
   * @generated
   * @ordered
   */
  protected String to = TO_EDEFAULT;

  /**
   * The default value of the '{@link #getDelim() <em>Delim</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDelim()
   * @generated
   * @ordered
   */
  protected static final String DELIM_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getDelim() <em>Delim</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDelim()
   * @generated
   * @ordered
   */
  protected String delim = DELIM_EDEFAULT;

  /**
   * The default value of the '{@link #getLimit() <em>Limit</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLimit()
   * @generated
   * @ordered
   */
  protected static final String LIMIT_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getLimit() <em>Limit</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLimit()
   * @generated
   * @ordered
   */
  protected String limit = LIMIT_EDEFAULT;

  /**
   * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected static final String VALUE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected String value = VALUE_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected LoadCsvImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return DslPackage.Literals.LOAD_CSV;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getPid()
  {
    return pid;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPid(String newPid)
  {
    String oldPid = pid;
    pid = newPid;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__PID, oldPid, pid));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSource()
  {
    return source;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSource(String newSource)
  {
    String oldSource = source;
    source = newSource;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__SOURCE, oldSource, source));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getTo()
  {
    return to;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTo(String newTo)
  {
    String oldTo = to;
    to = newTo;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__TO, oldTo, to));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getDelim()
  {
    return delim;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDelim(String newDelim)
  {
    String oldDelim = delim;
    delim = newDelim;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__DELIM, oldDelim, delim));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getLimit()
  {
    return limit;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLimit(String newLimit)
  {
    String oldLimit = limit;
    limit = newLimit;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__LIMIT, oldLimit, limit));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getValue()
  {
    return value;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setValue(String newValue)
  {
    String oldValue = value;
    value = newValue;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.LOAD_CSV__VALUE, oldValue, value));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case DslPackage.LOAD_CSV__PID:
        return getPid();
      case DslPackage.LOAD_CSV__SOURCE:
        return getSource();
      case DslPackage.LOAD_CSV__TO:
        return getTo();
      case DslPackage.LOAD_CSV__DELIM:
        return getDelim();
      case DslPackage.LOAD_CSV__LIMIT:
        return getLimit();
      case DslPackage.LOAD_CSV__VALUE:
        return getValue();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case DslPackage.LOAD_CSV__PID:
        setPid((String)newValue);
        return;
      case DslPackage.LOAD_CSV__SOURCE:
        setSource((String)newValue);
        return;
      case DslPackage.LOAD_CSV__TO:
        setTo((String)newValue);
        return;
      case DslPackage.LOAD_CSV__DELIM:
        setDelim((String)newValue);
        return;
      case DslPackage.LOAD_CSV__LIMIT:
        setLimit((String)newValue);
        return;
      case DslPackage.LOAD_CSV__VALUE:
        setValue((String)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case DslPackage.LOAD_CSV__PID:
        setPid(PID_EDEFAULT);
        return;
      case DslPackage.LOAD_CSV__SOURCE:
        setSource(SOURCE_EDEFAULT);
        return;
      case DslPackage.LOAD_CSV__TO:
        setTo(TO_EDEFAULT);
        return;
      case DslPackage.LOAD_CSV__DELIM:
        setDelim(DELIM_EDEFAULT);
        return;
      case DslPackage.LOAD_CSV__LIMIT:
        setLimit(LIMIT_EDEFAULT);
        return;
      case DslPackage.LOAD_CSV__VALUE:
        setValue(VALUE_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case DslPackage.LOAD_CSV__PID:
        return PID_EDEFAULT == null ? pid != null : !PID_EDEFAULT.equals(pid);
      case DslPackage.LOAD_CSV__SOURCE:
        return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
      case DslPackage.LOAD_CSV__TO:
        return TO_EDEFAULT == null ? to != null : !TO_EDEFAULT.equals(to);
      case DslPackage.LOAD_CSV__DELIM:
        return DELIM_EDEFAULT == null ? delim != null : !DELIM_EDEFAULT.equals(delim);
      case DslPackage.LOAD_CSV__LIMIT:
        return LIMIT_EDEFAULT == null ? limit != null : !LIMIT_EDEFAULT.equals(limit);
      case DslPackage.LOAD_CSV__VALUE:
        return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (pid: ");
    result.append(pid);
    result.append(", source: ");
    result.append(source);
    result.append(", to: ");
    result.append(to);
    result.append(", delim: ");
    result.append(delim);
    result.append(", limit: ");
    result.append(limit);
    result.append(", value: ");
    result.append(value);
    result.append(')');
    return result.toString();
  }

} //LoadCsvImpl
