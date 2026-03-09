// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_employee_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$CreateEmployeeRequestImpl _$$CreateEmployeeRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$CreateEmployeeRequestImpl(
      firstName: json['firstName'] as String,
      lastName: json['lastName'] as String,
      email: json['email'] as String,
      phone: json['phone'] as String?,
      department: json['department'] as String?,
      designation: json['designation'] as String?,
      joinDate: json['joinDate'] as String?,
    );

Map<String, dynamic> _$$CreateEmployeeRequestImplToJson(
        _$CreateEmployeeRequestImpl instance) =>
    <String, dynamic>{
      'firstName': instance.firstName,
      'lastName': instance.lastName,
      'email': instance.email,
      if (instance.phone case final value?) 'phone': value,
      if (instance.department case final value?) 'department': value,
      if (instance.designation case final value?) 'designation': value,
      if (instance.joinDate case final value?) 'joinDate': value,
    };
