// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_employee_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$UpdateEmployeeRequestImpl _$$UpdateEmployeeRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$UpdateEmployeeRequestImpl(
      firstName: json['firstName'] as String?,
      lastName: json['lastName'] as String?,
      phone: json['phone'] as String?,
      address: json['address'] as String?,
      department: json['department'] as String?,
      designation: json['designation'] as String?,
      joinDate: json['joinDate'] as String?,
    );

Map<String, dynamic> _$$UpdateEmployeeRequestImplToJson(
        _$UpdateEmployeeRequestImpl instance) =>
    <String, dynamic>{
      if (instance.firstName case final value?) 'firstName': value,
      if (instance.lastName case final value?) 'lastName': value,
      if (instance.phone case final value?) 'phone': value,
      if (instance.address case final value?) 'address': value,
      if (instance.department case final value?) 'department': value,
      if (instance.designation case final value?) 'designation': value,
      if (instance.joinDate case final value?) 'joinDate': value,
    };
